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

package org.scalasteward.core.gitlab

import org.scalasteward.core.git.Branch
import org.scalasteward.core.gitweb.data.Repo

// https://docs.gitlab.com/ce/api/
class Url(apiHost: String) {
  locally(apiHost)
  def branches(repo: Repo, branch: Branch): String =
    ???

  def forks(repo: Repo): String =
    ???

  def pulls(repo: Repo): String =
    // /projects/:id/merge_requests
    s"$apiHost/api/v4/projects/${repo.repo}/merge_requests"

  def repos(repo: Repo): String =
   // 'https://gitlab.example.com/api/v4/groups/smartobjects/projects'
    s"$apiHost/api/v4/groups/${repo.owner}/projects/${repo.repo}"
}
