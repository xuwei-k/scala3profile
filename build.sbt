import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

val commonSettings = Def.settings(
  scalafixOnCompile := true,
  publishTo := sonatypePublishToBundle.value,
  Compile / unmanagedResources += (LocalRootProject / baseDirectory).value / "LICENSE.txt",
  Compile / packageSrc / mappings ++= (Compile / managedSources).value.map { f =>
    (f, f.relativeTo((Compile / sourceManaged).value).get.getPath)
  },
  Compile / doc / scalacOptions ++= {
    val hash = sys.process.Process("git rev-parse HEAD").lineStream_!.head
    if (scalaBinaryVersion.value == "3") {
      Nil // TODO
    } else {
      Seq(
        "-sourcepath",
        (LocalRootProject / baseDirectory).value.getAbsolutePath,
        "-doc-source-url",
        s"https://github.com/xuwei-k/scala3profile/blob/${hash}€{FILE_PATH}.scala"
      )
    }
  },
  scalacOptions ++= {
    if (scalaBinaryVersion.value == "3") {
      Nil
    } else {
      Seq(
        "-Xsource:3",
      )
    }
  },
  scalacOptions ++= Seq(
    "-deprecation",
  ),
  pomExtra := (
    <developers>
    <developer>
      <id>xuwei-k</id>
      <name>Kenji Yoshida</name>
      <url>https://github.com/xuwei-k</url>
    </developer>
  </developers>
  <scm>
    <url>git@github.com:xuwei-k/scala3profile.git</url>
    <connection>scm:git:git@github.com:xuwei-k/scala3profile.git</connection>
  </scm>
  ),
  organization := "com.github.xuwei-k",
  homepage := Some(url("https://github.com/xuwei-k/scala3profile")),
  licenses := List(
    "MIT License" -> url("https://opensource.org/licenses/mit-license")
  ),
)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("set useSuperShell := false"),
  releaseStepCommandAndRemaining("publishSigned"),
  releaseStepCommandAndRemaining("sonatypeBundleRelease"),
  releaseStepCommandAndRemaining("set useSuperShell := true"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

commonSettings

publish / skip := true

lazy val sbtPlugin = projectMatrix
  .in(file("sbt-plugin"))
  .enablePlugins(SbtPlugin)
  .jvmPlatform(scalaVersions = Seq("2.12.21", "3.6.4"))
  .settings(
    commonSettings,
    description := "scala 3 profile sbt plugin",
    sbtTestDirectory := (LocalRootProject / baseDirectory).value / "test",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          (pluginCrossBuild / sbtVersion).value
        case _ =>
          "2.0.0-M4"
      }
    },
    Compile / sourceGenerators += task {
      val dir = (Compile / sourceManaged).value
      val className = "Scala3ProfileBuildInfo"
      val f = dir / "scala3profile" / s"${className}.scala"
      IO.write(
        f,
        Seq(
          "package scala3profile",
          "",
          s"private[scala3profile] object $className {",
          s"""  def version: String = "${version.value}" """,
          "}",
        ).mkString("", "\n", "\n")
      )
      Seq(f)
    },
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog := false,
    name := "sbt-scala3profile",
  )

lazy val compilerPlugin = project
  .in(file("compiler-plugin"))
  .settings(
    commonSettings,
    scalaVersion := "3.8.0",
    libraryDependencies += "org.scala-lang" %% "scala3-compiler" % scalaVersion.value,
    name := "scala3profile",
    description := "scala 3 profile compiler plugin",
  )

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.22"
