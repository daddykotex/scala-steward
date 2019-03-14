package org.scalasteward.core.vcs.gitlab

import io.circe.parser
import org.http4s.Uri
import org.scalasteward.core.vcs.data.PullRequestOut
import org.scalatest.{FunSuite, Matchers}

import scala.io.Source

class NewMergeRequestOutTest extends FunSuite with Matchers {
    import org.scalasteward.core.vcs.gitlab.http4s.GitlabJsonCodec._

    test("decode") {
      val input = Source.fromResource("gitlab/create-merge-request.json").mkString
      parser.decode[PullRequestOut](input) shouldBe
        Right(
          PullRequestOut(
            Uri.uri("https://gitlab.com/original-owner/dashboard-reporting/merge_requests/20"),
            "opened",
            "API MR"
          )
        )
    }
  }
