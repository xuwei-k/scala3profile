def scala3 = "3.6.1-RC1-bin-20241014-69ac5fb-NIGHTLY"

TaskKey[Unit]("check") := {
  val json: String = IO.read(target.value / "scala3profile-aggregate.json")
  Seq(
    "T1.scala",
    "A2.scala",
    "A3.scala",
    "T2.scala",
  ).foreach { x =>
    assert(json contains x, json)
  }
}

val a1 = project.settings(
  scalaVersion := scala3
)

val a2 = project
  .settings(
    scalaVersion := scala3
  )
  .dependsOn(a1)

val a3 = project.settings(
)
