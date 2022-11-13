package scala3profile

import dotty.tools.dotc.CompilationUnit
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Phases.Phase
import dotty.tools.dotc.plugins.ResearchPlugin
import dotty.tools.dotc.report
import java.io.File

class Scala3Profile extends ResearchPlugin {
  override def name = "scala3profile"
  override def description = "profiler"

  private[this] def stringToPosInt: String => Option[Long] = _.toLongOption.filter(_ > 0L)
  private[this] val StringToPosLongExtractor: PartialFunction[String, Long] = stringToPosInt.unlift

  override def init(options: List[String], phasesList: List[List[Phase]])(using c: Context): List[List[Phase]] = {
    report.echo("options = " + options.mkString(", "))
    val outputDir = options.collectFirst { case s"output-dir:${fileName}" =>
      new File(fileName)
    }.getOrElse(
      sys.error("please set output-dir")
    )

    val minTime = options.collectFirst { case s"min-time:${StringToPosLongExtractor(m)}" =>
      m * 1000L
    }.getOrElse(0L)

    val categories = options.collect { case s"categories:$c" => c }
    val phases = options.collect { case s"phases:${phase}" => phase }.toSet
    val baseName =
      options.collectFirst { case s"output-base-name:${x}" => x }.getOrElse(sys.error("please set output-base-name"))

    phasesList.map {
      _.map {
        case phase if phase.phaseName == "inlining" && phases.contains(phase.phaseName) =>
          report.echo("replace inlining phase " + outputDir)
          val outputFile = new File(outputDir, baseName + "-inlining.json")
          new ProfileInlining(outputFile = outputFile, minTime = minTime, categories = categories)
        case phase if phase.phaseName == "typer" && phases.contains(phase.phaseName) =>
          report.echo("replace typer phase " + outputDir)
          val outputFile = new File(outputDir, baseName + "-typer.json")
          new ProfileTyper(outputFile = outputFile, minTime = minTime, categories = categories)
        case other =>
          other
      }
    }
  }
}
