package org.scalasteward.core.vcs.github.http4s

import org.http4s.Uri
import org.scalasteward.core.git.Branch
import org.scalasteward.core.vcs.github.GitHubUrl
import org.scalasteward.core.vcs.data.Repo
import org.scalatest.{FunSuite, Matchers}

class http4SGitHubUrlTest extends FunSuite with Matchers {
  val http4sUrl = new GitHubUrl(Uri.uri("https://api.github.com"))
  import http4sUrl._

  type Result[A] = Either[Throwable, A]
  val repo = Repo("fthomas", "refined")
  val branch = Branch("master")

  test("branches") {
    branches(repo, branch) shouldBe
      Uri.uri("https://api.github.com/repos/fthomas/refined/branches/master")
  }

  test("forks") {
    forks(repo) shouldBe
      Uri.uri("https://api.github.com/repos/fthomas/refined/forks")
  }

  test("listPullRequests") {
    listPullRequests(repo, "scala-steward:update/fs2-core-1.0.0") shouldBe
      Uri.uri("https://api.github.com/repos/fthomas/refined/pulls?head=scala-steward%3Aupdate/fs2-core-1.0.0&state=all")
  }

  test("pulls") {
    pulls(repo) shouldBe
      Uri.uri("https://api.github.com/repos/fthomas/refined/pulls")
  }

  test("repos") {
    repos(repo) shouldBe
      Uri.uri("https://api.github.com/repos/fthomas/refined")
  }
}
