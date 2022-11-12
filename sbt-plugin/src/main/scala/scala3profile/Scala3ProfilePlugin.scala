package scala3profile

import sbt.*
import sbt.Keys.*
import sjsonnew.shaded.scalajson.ast.unsafe.JArray
import sjsonnew.shaded.scalajson.ast.unsafe.JValue
import sjsonnew.support.scalajson.unsafe.Parser.parseFromFile
import sjsonnew.support.scalajson.unsafe.PrettyPrinter

object Scala3ProfilePlugin extends AutoPlugin {
  object autoImport {
    val scala3profileMinTime = settingKey[Long]("")
    val scala3profileOutputDir = settingKey[File]("")
    val scala3profileOutputBaseName = settingKey[String]("")
    val scala3profilePhases = settingKey[Seq[String]]("")
    val scala3profileAggregate = taskKey[ResultValue]("")
    val scala3profileCategories = taskKey[Seq[String]]("")
    val scala3profileAggregateConfigurations = settingKey[Seq[Configuration]]("")
  }
  import autoImport.*

  override def trigger: PluginTrigger = allRequirements

  private[this] type ResultValue = List[JValue]

  private[this] def defaultConfigurations = Seq(Compile, Test)

  override def buildSettings: Seq[Def.Setting[?]] = Def.settings(
    scala3profileAggregateConfigurations := defaultConfigurations,
    LocalRootProject / scala3profileAggregate / scala3profileOutputDir := (LocalRootProject / target).value,
    LocalRootProject / scala3profileAggregate := {
      val extracted = Project.extract(state.value)
      val currentBuildUri = extracted.currentRef.build
      val result =
        extracted.structure.units
          .apply(currentBuildUri)
          .defined
          .values
          .flatMap { p =>
            val proj = LocalProject(p.id)
            for {
              c <- extracted.getOpt(proj / scala3profileAggregateConfigurations).toList.flatten
              dir <- extracted.getOpt(proj / c / scala3profileOutputDir).toList
              phases <- extracted.getOpt(proj / c / scala3profilePhases).toList
              baseName <- extracted.getOpt(proj / c / scala3profileOutputBaseName).toList
              phase <- phases
            } yield {
              dir / s"${baseName}-${phase}.json"
            }
          }
          .filter(_.isFile)
          .map(parseFromFile(_).get)
          .flatMap(j => j.asInstanceOf[JArray].value)
          .toList

      val f =
        (LocalRootProject / scala3profileAggregate / scala3profileOutputDir).value / "scala3profile-aggregate.json"

      IO.write(f, PrettyPrinter(JArray(result.toArray)))
      result
    }
  )

  private[this] val enable: Def.Initialize[Boolean] = Def.setting(
    scalaBinaryVersion.value == "3" && scalaVersion.value.contains("NIGHTLY")
  )

  def scala3profileSetting(c: Configuration): SettingsDefinition =
    Def.settings(
      c / scala3profileOutputDir := target.value,
      c / scala3profileOutputBaseName := s"scala3profile-${c.name}",
      c / scala3profilePhases := Seq("typer", "inlining"),
      c / scala3profileCategories := Seq(
        s"sbt-scope-${c.name}",
        s"sbt-project-${name.value}",
      ),
      (c / compile / scalacOptions) ++= {
        val s = streams.value
        if (enable.value) {
          val prefix = "-P:scala3profile:"
          val x = (c / scala3profileOutputDir).value
          val outDir: String =
            (LocalRootProject / baseDirectory).value.relativize(x).fold(x.getAbsolutePath)(_.toString)
          val baseName: String = (c / scala3profileOutputBaseName).value

          (c / scala3profilePhases).value.map { p =>
            s"${prefix}phases:${p}"
          }.toList ::: List[String](
            s"${prefix}output-dir:${outDir}",
            s"${prefix}output-base-name:${baseName}"
          ) ::: scala3profileMinTime.?.value.map { x =>
            s"${prefix}min-time:${x}"
          }.toList ::: (c / scala3profileCategories).?.value.toList.flatten.map { (value: String) =>
            s"${prefix}categories:${value}"
          }
        } else {
          if (scalaBinaryVersion.value == "3") {
            s.log.info(
              s"scala3profile plugin disabled because scala version in ${scalaVersion.value}. please use NIGHTLY version"
            )
          }
          Nil
        }
      }
    )

  override def projectSettings: Seq[Def.Setting[?]] = Def.settings(
    libraryDependencies ++= {
      if (enable.value) {
        Seq(compilerPlugin("com.github.xuwei-k" %% "scala3profile" % Scala3ProfileBuildInfo.version))
      } else {
        Nil
      }
    }
  ) ++ defaultConfigurations.flatMap(scala3profileSetting)
}
