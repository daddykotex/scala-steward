package org.scalasteward.core.vcs.github

import io.circe.parser
import org.scalasteward.core.git.Sha1.HexString
import org.scalasteward.core.git.{Branch, Sha1}
import org.scalasteward.core.vcs.data.{BranchOut, CommitOut}
import org.scalatest.{FunSuite, Matchers}

import scala.io.Source

class BranchOutTest extends FunSuite with Matchers {
  import org.scalasteward.core.vcs.github.http4s.GitHubJsonCodec._

  test("decode") {
    val input = Source.fromResource("github/get-branch.json").mkString
    parser.decode[BranchOut](input) shouldBe
      Right(
        BranchOut(
          Branch("master"),
          CommitOut(Sha1(HexString("7fd1a60b01f91b314f59955a4e4d4e80d8edf11d")))
        )
      )
  }
}
