package org.scalasteward.core

import org.scalasteward.core.application.Config
import org.scalasteward.core.model.Update
import org.scalasteward.core.vcs.data.Repo

package object vcs {

  def getLogin(config: Config, repo: Repo): String =
    if (config.doNotFork) repo.owner else config.gitHubLogin

  def headFor(login: String, update: Update): String =
    s"$login:${git.branchFor(update).name}"
}
