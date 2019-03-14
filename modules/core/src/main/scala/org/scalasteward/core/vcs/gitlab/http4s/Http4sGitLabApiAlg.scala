/*
 * Copyright 2018-2019 scala-steward contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalasteward.core.vcs.gitlab.http4s

import cats.effect.Sync
import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._
import org.http4s.Method.{GET, POST}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Header, Headers, Request, Status, Uri}
import org.scalasteward.core.application.Config
import org.scalasteward.core.git
import org.scalasteward.core.git.{Branch, Sha1}
import org.scalasteward.core.model.Update
import org.scalasteward.core.vcs.data._
import org.scalasteward.core.vcs.VCSApiAlg
import org.scalasteward.core.vcs.gitlab._
import org.scalasteward.core.util.uri.uriDecoder

// {"id": "scala-steward%2Fdashboard-reporting", "namespace": "scala-steward"}
final case class ForkPayload(id: String, namespace: String)
// {"id": "scala-steward%2Fdashboard-reporting", "target_project_id": 3148, "source_branch": "pull-request", "target_branch": "master", "title": "API MR", "description": "body"}
final case class MergeRequestPayload(id: String, title: String, description: String, target_project_id: Long, source_branch: String, target_branch: Branch)
object MergeRequestPayload {
  def apply(id: String, projectId: Long, data: NewPullRequestData): MergeRequestPayload =
    MergeRequestPayload(id, data.title, data.body, projectId, data.head, data.base)
}
final case class MergeRequestOut(web_url: Uri, state: String, title: String) {
  val pullRequestOut: PullRequestOut = PullRequestOut(web_url, state, title)
}
final case class CommitId(id: Sha1) {
  val commitOut: CommitOut = CommitOut(id)
}
final case class ProjectId(id: Long)
private final case class NamespaceOut(name: String)

object GitlabJsonCodec {
  // prevent IntelliJ from removing the import of uriDecoder
  locally(uriDecoder)

  implicit val forkPayloadEncoder: Encoder[ForkPayload] = deriveEncoder
  private val fromOwner: Decoder[UserOut] = Decoder.instance {
    _.downField("username").as[String].map(UserOut)
  }
  private val viaNamespace: Decoder[UserOut] = deriveDecoder[NamespaceOut].map(ns => UserOut(ns.name))
  private val fromNamespace: Decoder[UserOut] = Decoder.instance {c =>
    c.downField("namespace").as[UserOut](viaNamespace)
  }

  implicit val userOutDecoder: Decoder[UserOut] = fromOwner.or(fromNamespace)

  val parentRepoOutDecoder: Decoder[RepoOut] = Decoder.instance { c =>
    for {
      name <- c.downField("name").as[String]
      owner <- userOutDecoder(c)
      cloneUrl <- c.downField("http_url_to_repo").as[Uri]
      defaultBranch <- c.downField("default_branch").as[Option[Branch]].map(_.getOrElse(Branch("master")))
    } yield RepoOut(name, owner, None, cloneUrl, defaultBranch)
  }
  implicit val repoOutDecoder: Decoder[RepoOut] = Decoder.instance { c =>
    for {
      name <- c.downField("name").as[String]
      owner <- userOutDecoder(c)
      cloneUrl <- c.downField("http_url_to_repo").as[Uri]
      parent <- c.downField("forked_from_project").as[Option[RepoOut]](Decoder.decodeOption(parentRepoOutDecoder))
      defaultBranch <- c.downField("default_branch").as[Option[Branch]].map(_.getOrElse(Branch("master")))
    } yield RepoOut(name, owner, parent, cloneUrl, defaultBranch)
  }

  implicit val projectIdDecoder: Decoder[ProjectId] = Decoder[Long].map(ProjectId)
  implicit val mergeRequestPayloadEncoder: Encoder[MergeRequestPayload] = deriveEncoder
  implicit val PullRequestOutDecoder: Decoder[PullRequestOut] = deriveDecoder[MergeRequestOut].map(_.pullRequestOut)
  implicit val commitOutDecoder: Decoder[CommitOut] = deriveDecoder[CommitId].map(_.commitOut)
  implicit val branchOutDecoder: Decoder[BranchOut] = deriveDecoder[BranchOut]
}

class Http4sGitLabApiAlg[F[_]](
    implicit
    client: Client[F],
    config: Config,
    user: AuthenticatedUser,
    F: Sync[F]
) extends VCSApiAlg[F] {
  import Http4sGitLabApiAlg._
  import GitlabJsonCodec._

  val url = new GitLabUrl(config.gitHubApiHost)

  override def sourceFor(repo: Repo, update: Update): String =
    git.branchFor(update).name

  override def createFork(repo: Repo): F[RepoOut] = {
    val userOwnedRepo = repo.copy(owner = user.login)
    val data = ForkPayload(url.encodedProjectId(userOwnedRepo), user.login)
    val req = Request[F](POST, url.createFork(repo)).withEntity(data)(jsonEncoderOf)
    expectJsonOf[RepoOut](req) handleErrorWith {
      case UnexpectedStatus(Status.Conflict) => getRepo(userOwnedRepo)
    }
  }

  override def createPullRequest(repo: Repo, data: NewPullRequestData): F[PullRequestOut] = {
    val userOwnedRepo = repo.copy(owner = user.login)
    for {
      projectId <- get[ProjectId](url.repos(repo))
      payload = MergeRequestPayload(url.encodedProjectId(userOwnedRepo), projectId.id, data)
      req = Request[F](POST, url.createMergeRequest(repo)).withEntity(payload)(jsonEncoderOf)
      res <- expectJsonOf[PullRequestOut](req)
    } yield res
  }

  override def getBranch(repo: Repo, branch: Branch): F[BranchOut] =
    get(url.getBranch(repo, branch))

  override def getRepo(repo: Repo): F[RepoOut] =
    get(url.repos(repo))

  override def listPullRequests(repo: Repo, head: String): F[List[PullRequestOut]] =
    get(url.listMergeRequests(repo, head))

  private def get[A: Decoder](uri: Uri): F[A] =
    expectJsonOf[A](Request[F](GET, uri))

  private def expectJsonOf[A: Decoder](req: Request[F]): F[A] =
    client.expect[A](authenticate(user)(req))(jsonOf)
}

object Http4sGitLabApiAlg {
  def authenticate[F[_]](user: AuthenticatedUser)(req: Request[F]): Request[F] =
    req.withHeaders(req.headers ++ Headers(Header("Private-Token", user.accessToken)))
}
