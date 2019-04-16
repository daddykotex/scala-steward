package org.scalasteward.core.vcs

import cats.implicits._
import org.http4s.Uri
import org.scalasteward.core.application.Config
import org.scalasteward.core.git.GitAlg
import org.scalasteward.core.util
import org.scalasteward.core.util.MonadThrowable
import org.scalasteward.core.vcs.data.{Repo, RepoOut}

trait VCSRepoAlg[F[_]] {
  def clone(repo: Repo, repoOut: RepoOut): F[Unit]

  def syncFork(repo: Repo, repoOut: RepoOut): F[RepoOut]
}

object VCSRepoAlg {
  def create[F[_]: MonadThrowable](config: Config, gitAlg: GitAlg[F]): VCSRepoAlg[F] =
    new VCSRepoAlg[F] {
      override def clone(repo: Repo, repoOut: RepoOut): F[Unit] =
        for {
          _ <- gitAlg.clone(repo, withLogin(repoOut.clone_url))
          _ <- gitAlg.setAuthor(repo, config.gitAuthor)
        } yield ()

      override def syncFork(repo: Repo, repoOut: RepoOut): F[RepoOut] =
        if (config.doNotFork) repoOut.pure[F]
        else {
          for {
            parent <- repoOut.parentOrRaise[F]
            _ <- gitAlg.syncFork(repo, withLogin(parent.clone_url), parent.default_branch)
          } yield parent
        }

      val withLogin: Uri => Uri =
        util.uri.withUserInfo.set(config.gitHubLogin)
    }
}
