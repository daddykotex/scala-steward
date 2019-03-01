package org.scalasteward.core.update

import better.files.File
import cats.implicits._
import org.scalasteward.core.vcs.github.data.Repo
import org.scalasteward.core.mock.MockContext.filterAlg
import org.scalasteward.core.mock.MockState
import org.scalasteward.core.model.Update
import org.scalasteward.core.repoconfig.{RepoConfig, UpdatePattern, UpdatesConfig}
import org.scalasteward.core.update.FilterAlg.BadVersions
import org.scalasteward.core.util.Nel
import org.scalatest.{FunSuite, Matchers}

class FilterAlgTest extends FunSuite with Matchers {
  test("removeBadVersions: update without bad version") {
    val update = Update.Single("com.jsuereth", "sbt-pgp", "1.1.0", Nel.of("1.1.2", "2.0.0"))
    FilterAlg.removeBadVersions(update) shouldBe Right(update)
  }

  test("removeBadVersions: update with bad version") {
    val update = Update.Single("com.jsuereth", "sbt-pgp", "1.1.2-1", Nel.of("1.1.2", "2.0.0"))
    FilterAlg.removeBadVersions(update) shouldBe Right(update.copy(newerVersions = Nel.of("2.0.0")))
  }

  test("removeBadVersions: update with only bad versions") {
    val update = Update.Single("org.http4s", "http4s-dsl", "0.18.0", Nel.of("0.19.0"))
    FilterAlg.removeBadVersions(update) shouldBe Left(BadVersions(update))
  }

  test("ignore update via config updates.ignore") {
    val repo = Repo("fthomas", "scala-steward")
    val update1 = Update.Single("org.http4s", "http4s-dsl", "0.17.0", Nel.of("0.18.0"))
    val update2 = Update.Single("eu.timepit", "refined", "0.8.0", Nel.of("0.8.1"))

    val configFile = File("/tmp/ws/fthomas/scala-steward/.scala-steward.conf")
    val configContent =
      """updates.ignore = [ { groupId = "eu.timepit", artifactId = "refined" } ]"""

    val initialState = MockState.empty.add(configFile, configContent)
    val (state, filtered) =
      filterAlg.localFilterMany(repo, List(update1, update2)).run(initialState).unsafeRunSync()

    filtered shouldBe List(update1)
    state shouldBe initialState.copy(
      commands = Vector(List("read", configFile.pathAsString)),
      logs = Vector((None, "Ignore eu.timepit:refined : 0.8.0 -> 0.8.1"))
    )
  }

  test("ignore update via config updates.allow") {
    val update1 = Update.Single("org.http4s", "http4s-dsl", "0.17.0", Nel.of("0.18.0"))
    val update2 = Update.Single("eu.timepit", "refined", "0.8.0", Nel.of("0.8.1"))

    val config = RepoConfig(
      updates = Some(
        UpdatesConfig(
          allow = Some(
            List(
              UpdatePattern("org.http4s", None, Some("0.17")),
              UpdatePattern("eu.timepit", Some("refined"), Some("0.8"))
            )
          )
        )
      )
    )

    val filtered = filterAlg
      .localFilterManyWithConfig(config, List(update1, update2))
      .runA(MockState.empty)
      .unsafeRunSync()

    filtered shouldBe List(update2)
  }
}
