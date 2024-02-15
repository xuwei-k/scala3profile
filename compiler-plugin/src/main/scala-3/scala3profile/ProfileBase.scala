package scala3profile

import dotty.tools.dotc.CompilationUnit
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Phases
import dotty.tools.dotc.report
import dotty.tools.dotc.transform.Inlining
import dotty.tools.dotc.typer.TyperPhase
import scala.collection.concurrent.TrieMap
import java.io.File
import java.nio.file.Files
import java.nio.charset.StandardCharsets

trait ProfileBase extends Phases.Phase {

  protected def outputFile: File
  protected def minTime: Long

  protected def log: Logger

  protected def categories: Seq[String]

  private val compileTimeResults: TrieMap[String, Time] = TrieMap.empty[String, Time]

  override final def runOn(units: List[CompilationUnit])(using Context): List[CompilationUnit] = {
    log.debug(s"start ${phaseName} profile " + outputFile)
    val result = super.runOn(units)
    log.debug(s"end ${phaseName} profile " + outputFile)
    val list = compileTimeResults.toList
    compileTimeResults.clear()
    if (list.isEmpty) {
      log.debug(s"[${phaseName} profile] there is no result " + outputFile)
    }

    val base = new File(".").getAbsoluteFile.toPath
    val threadId = Thread.currentThread().getId
    val json = list.map { (path, time) =>
      val fileName = base.relativize(new File(path).getAbsoluteFile.toPath).toFile.getName
      Common.jsonLine(
        name = fileName,
        category = phaseName :: categories.toList,
        time = time,
        threadId = threadId
      )
    }.mkString("[\n", ",\n", "\n]")
    log.debug("write to " + outputFile)
    Files.write(outputFile.toPath, json.getBytes(StandardCharsets.UTF_8))
    result
  }

  abstract override final def run(using c: Context): Unit = {
    val start = Common.microTime()
    super.run
    val end = Common.microTime()
    val duration = end - start
    if (duration > minTime) {
      compileTimeResults += (c.compilationUnit.source.path -> Time(start = start, duration = duration))
    }
  }
}
