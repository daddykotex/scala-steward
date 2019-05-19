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

package org.scalasteward.core.vcs

import cats.Monad
import cats.implicits._
import org.scalasteward.core.application.Config
import org.scalasteward.core.git.Branch
import org.scalasteward.core.vcs.data._
import org.scalasteward.core.util.MonadThrowable

trait VCSApiAlg[F[_]] {

  def createFork(repo: Repo)(implicit F: MonadThrowable[F]): F[RepoOut]

  def createPullRequest(repo: Repo, data: NewPullRequestData)(
      implicit F: Monad[F]
  ): F[PullRequestOut]

  def getBranch(repo: Repo, branch: Branch): F[BranchOut]

  def getRepo(repo: Repo): F[RepoOut]

  def listPullRequests(repo: Repo, head: String, base: Branch): F[List[PullRequestOut]]

  def getDefaultBranch(repoOut: RepoOut): F[BranchOut] =
    getBranch(repoOut.repo, repoOut.default_branch)

  def createForkOrGetRepo(config: Config, repo: Repo)(implicit F: MonadThrowable[F]): F[RepoOut] =
    if (config.doNotFork) getRepo(repo)
    else createFork(repo)

  def createForkOrGetRepoWithDefaultBranch(config: Config, repo: Repo)(
      implicit F: MonadThrowable[F]
  ): F[(RepoOut, BranchOut)] =
    if (config.doNotFork) getRepoWithDefaultBranch(repo)
    else createForkWithDefaultBranch(repo)

  def createForkWithDefaultBranch(repo: Repo)(
      implicit F: MonadThrowable[F]
  ): F[(RepoOut, BranchOut)] =
    for {
      fork <- createFork(repo)
      parent <- fork.parentOrRaise[F]
      branchOut <- getDefaultBranch(parent)
    } yield (fork, branchOut)

  def getRepoWithDefaultBranch(repo: Repo)(
      implicit F: Monad[F]
  ): F[(RepoOut, BranchOut)] =
    for {
      repoOut <- getRepo(repo)
      branchOut <- getDefaultBranch(repoOut)
    } yield (repoOut, branchOut)
}
