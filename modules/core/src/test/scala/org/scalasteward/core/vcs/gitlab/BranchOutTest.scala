package org.scalasteward.core.vcs.gitlab

import io.circe.parser
import org.scalasteward.core.git.{Branch, Sha1}
import org.scalasteward.core.vcs.data.{BranchOut, CommitOut}
import org.scalatest.{FunSuite, Matchers}

import scala.io.Source

class BranchOutTest extends FunSuite with Matchers {
  import org.scalasteward.core.vcs.gitlab.http4s.GitlabJsonCodec._

  test("decode") {
    val input = Source.fromResource("gitlab/get-branch.json").mkString
    parser.decode[BranchOut](input) shouldBe
      Right(
        BranchOut(
          Branch("pull-request"),
          CommitOut(Sha1.from("014cc72ad4a9022cadf8c72ed238443c450ee44a").getOrElse(fail("invalid sha")))
        )
      )
  }
}