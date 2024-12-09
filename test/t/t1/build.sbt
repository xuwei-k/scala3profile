def scala3 = "3.6.4-RC1-bin-20241205-c61897d-NIGHTLY"

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

val root = project
  .in(file("."))
  .settings(
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
  )
  .aggregate(
    a1,
    a2,
    a3
  )
