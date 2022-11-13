package scala3profile

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.CompilationUnit
import dotty.tools.dotc.report
import dotty.tools.dotc.typer.TyperPhase
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.collection.concurrent.TrieMap

class ProfileTyper(
  outputFile: File,
  minTime: Long,
  categories: Seq[String],
  log: Logger
) extends TyperPhase {

  private[this] val enterSymsResult: TrieMap[String, Time] = TrieMap.empty[String, Time]
  private[this] val typeCheckResult: TrieMap[String, Time] = TrieMap.empty[String, Time]
  private[this] val javaCheckResult: TrieMap[String, Time] = TrieMap.empty[String, Time]

  private val allResults: List[(String, TrieMap[String, Time])] = List(
    ("enter-sym", enterSymsResult),
    ("java-check", javaCheckResult),
    ("type-check", typeCheckResult)
  )

  override final def runOn(units: List[CompilationUnit])(using Context): List[CompilationUnit] = {
    log.debug(s"start ${phaseName} profile " + outputFile)
    val result = super.runOn(units)
    log.debug(s"end ${phaseName} profile " + outputFile)

    val base = new File(".").getAbsoluteFile.toPath
    val threadId = Thread.currentThread().getId
    val json = allResults.flatMap { case (category, values) =>
      values.map { (path, time) =>
        val fileName = base.relativize(new File(path).getAbsoluteFile.toPath).toFile.getName
        Common.jsonLine(
          name = fileName,
          category = category :: "typer" :: categories.toList,
          time = time,
          threadId = threadId
        )
      }
    }.mkString("[\n", ",\n", "\n]")
    log.debug("write to " + outputFile)
    Files.write(outputFile.toPath, json.getBytes(StandardCharsets.UTF_8))
    result
  }

  override final def enterSyms(using c: Context): Unit = {
    val start = Common.microTime()
    super.enterSyms
    val end = Common.microTime()
    val duration = end - start
    if (duration > minTime) {
      enterSymsResult += (c.compilationUnit.source.path -> Time(start = start, duration = duration))
    }
  }

  override final def typeCheck(using c: Context): Unit = {
    val start = Common.microTime()
    super.typeCheck
    val end = Common.microTime()
    val duration = end - start
    if (duration > minTime) {
      typeCheckResult += (c.compilationUnit.source.path -> Time(start = start, duration = duration))
    }
  }

  override final def javaCheck(using c: Context): Unit = {
    val start = Common.microTime()
    super.javaCheck
    val end = Common.microTime()
    val duration = end - start
    if (duration > minTime) {
      javaCheckResult += (c.compilationUnit.source.path -> Time(start = start, duration = duration))
    }
  }
}
