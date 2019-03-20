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

package org.scalasteward.core.vcs.github.http4s

import cats.effect.Sync
import io.circe._
import io.circe.generic.semiauto._
import org.http4s.Method.{GET, POST}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{BasicCredentials, Headers, Request, Uri}
import org.scalasteward.core.application.Config
import org.scalasteward.core.{git, vcs}
import org.scalasteward.core.git.Branch
import org.scalasteward.core.model.Update
import org.scalasteward.core.util.uri.uriDecoder
import org.scalasteward.core.vcs.data._
import org.scalasteward.core.vcs.VCSApiAlg
import org.scalasteward.core.vcs.github._
import org.scalasteward.core.vcs.github.http4s.Http4sGitHubApiAlg._

object GitHubJsonCodec {
  // prevent IntelliJ from removing the import of uriDecoder
  locally(uriDecoder)

  implicit val repoOutDecoder: Decoder[RepoOut] =
    deriveDecoder

  implicit val userOutDecoder: Decoder[UserOut] =
    deriveDecoder

  implicit val pullRequestOutDecoder: Decoder[PullRequestOut] =
    deriveDecoder

  implicit val newPullRequestDataEncoder: Encoder[NewPullRequestData] =
    deriveEncoder

  implicit val commitOutDecoder: Decoder[CommitOut] =
    deriveDecoder

  implicit val branchOutDecoder: Decoder[BranchOut] =
    deriveDecoder
}

class Http4sGitHubApiAlg[F[_]](
    implicit
    client: Client[F],
    config: Config,
    user: AuthenticatedUser,
    F: Sync[F]
) extends VCSApiAlg[F] {
  import GitHubJsonCodec._

  val url = new GitHubUrl(config.gitHubApiHost)

  override def sourceFor(repo: Repo, update: Update): String =
    s"${vcs.getLogin(config, repo)}:${git.branchFor(update).name}"

  override def createFork(repo: Repo): F[RepoOut] = {
    val req = Request[F](POST, url.forks(repo))
    expectJsonOf[RepoOut](req)
  }

  override def createPullRequest(repo: Repo, data: NewPullRequestData): F[PullRequestOut] = {
    val req = Request[F](POST, url.pulls(repo)).withEntity(data)(jsonEncoderOf)
    expectJsonOf[PullRequestOut](req)
  }

  override def getBranch(repo: Repo, branch: Branch): F[BranchOut] =
    get[BranchOut](url.branches(repo, branch))

  override def getRepo(repo: Repo): F[RepoOut] =
    get[RepoOut](url.repos(repo))

  override def listPullRequests(repo: Repo, head: String): F[List[PullRequestOut]] =
    get[List[PullRequestOut]](url.listPullRequests(repo, head))

  def get[A: Decoder](uri: Uri): F[A] =
    expectJsonOf[A](Request[F](GET, uri))

  def expectJsonOf[A: Decoder](req: Request[F]): F[A] =
    client.expect[A](authenticate(user)(req))(jsonOf)
}

object Http4sGitHubApiAlg {
  def authenticate[F[_]](user: AuthenticatedUser)(req: Request[F]): Request[F] =
    req.withHeaders(req.headers ++ Headers(toBasicAuth(user)))

  def toBasicAuth(user: AuthenticatedUser): Authorization =
    Authorization(BasicCredentials(user.login, user.accessToken))
}
