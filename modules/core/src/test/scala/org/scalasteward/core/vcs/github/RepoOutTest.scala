package org.scalasteward.core.vcs.github

import cats.effect.IO
import io.circe.parser
import org.http4s.Uri
import org.scalasteward.core.git.Branch
import org.scalasteward.core.vcs.data.{Repo, RepoOut, UserOut}
import org.scalatest.{FunSuite, Matchers}

import scala.io.Source

class RepoOutTest extends FunSuite with Matchers {
  import org.scalasteward.core.vcs.github.http4s.GitHubJsonCodec._

  val parent =
    RepoOut(
      "base.g8",
      UserOut("ChristopherDavenport"),
      None,
      Uri.uri("https://github.com/ChristopherDavenport/base.g8.git"),
      Branch("master")
    )

  val fork =
    RepoOut(
      "base.g8-1",
      UserOut("scala-steward"),
      Some(parent),
      Uri.uri("https://github.com/scala-steward/base.g8-1.git"),
      Branch("master")
    )

  test("decode") {
    val input = Source.fromResource("github/create-fork.json").mkString
    parser.decode[RepoOut](input) shouldBe Right(fork)
  }

  test("parentOrRaise") {
    fork.parentOrRaise[IO].unsafeRunSync() shouldBe parent
  }

  test("repo") {
    fork.repo shouldBe Repo("scala-steward", "base.g8-1")
  }
}
