package org.scalasteward.core.vcs.gitlab

import cats.effect.IO
import io.circe.parser
import org.http4s.Uri
import org.scalasteward.core.git.Branch
import org.scalasteward.core.vcs.data.{Repo, RepoOut, UserOut}
import org.scalatest.{FunSuite, Matchers}

import scala.io.Source

class RepoOutTest extends FunSuite with Matchers {
    import org.scalasteward.core.vcs.gitlab.http4s.GitlabJsonCodec._

    val parent =
      RepoOut(
        "dashboard-reporting",
        UserOut("original-owner"),
        None,
        Uri.uri("https://gitlab.com/original-owner/dashboard-reporting.git"),
        Branch("master")
      )

    val fork =
      RepoOut(
        "dashboard-reporting",
        UserOut("scala-steward"),
        Some(parent),
        Uri.uri("https://gitlab.com/scala-steward/dashboard-reporting.git"),
        Branch("master")
      )

    val getRepo =
      RepoOut(
        "dashboard-reporting",
        UserOut("dataviz"),
        None,
        Uri.uri("https://gitlab.com/dataviz/dashboard-reporting.git"),
        Branch("master")
      )

    test("decode create fork") {
      val input = Source.fromResource("gitlab/create-fork.json").mkString
      parser.decode[RepoOut](input) shouldBe Right(fork)
    }


    test("decode get repo") {
      val input = Source.fromResource("gitlab/repo-out.json").mkString
      parser.decode[RepoOut](input) shouldBe Right(getRepo)
    }

    test("parentOrRaise") {
      fork.parentOrRaise[IO].unsafeRunSync() shouldBe parent
    }

    test("repo") {
      fork.repo shouldBe Repo("scala-steward", "dashboard-reporting")
    }
  }
