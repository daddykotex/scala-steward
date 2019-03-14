package org.scalasteward.core.vcs.gitlab

import io.circe.syntax._
import org.scalasteward.core.git
import org.scalasteward.core.git.{Branch, Sha1}
import org.scalasteward.core.model.Update
import org.scalasteward.core.nurture.UpdateData
import org.scalasteward.core.util.Nel
import org.scalasteward.core.vcs.data.{NewPullRequestData, Repo}
import org.scalasteward.core.vcs.gitlab.http4s.MergeRequestPayload
import org.scalatest.{FunSuite, Matchers}

class NewMergeRequestDataTest extends FunSuite with Matchers {
    import org.scalasteward.core.vcs.gitlab.http4s.GitlabJsonCodec._

    test("asJson") {
      val data = UpdateData(
        Repo("foo", "bar"),
        Update.Single("ch.qos.logback", "logback-classic", "1.2.0", Nel.of("1.2.3")),
        Branch("master"),
        Sha1(Sha1.HexString("d6b6791d2ea11df1d156fe70979ab8c3a5ba3433")),
        Branch("update/logback-classic-1.2.3")
      )
      val login = "scala-steward"
      val requestData = NewPullRequestData.from(data, login, (_, _) => git.branchFor(data.update).name)

      MergeRequestPayload("scala-steward%2Fbar", 123L, requestData)
        .asJson
        .spaces2 shouldBe
        """|{
           |  "id" : "scala-steward%2Fbar",
           |  "title" : "Update logback-classic to 1.2.3",
           |  "description" : "Updates ch.qos.logback:logback-classic from 1.2.0 to 1.2.3.\n\nI'll automatically update this PR to resolve conflicts as long as you don't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you have any feedback, just mention @scala-steward in the comments below.\n\nHave a nice day!\n\n<details>\n<summary>Ignore future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of this dependency:\n```\nupdates.ignore = [{ groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }]\n```\n</details>",
           |  "target_project_id" : 123,
           |  "source_branch" : "update/logback-classic-1.2.3",
           |  "target_branch" : "master"
           |}
           |""".stripMargin.trim
    }
  }
