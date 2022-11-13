package scala3profile

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.report

final class Logger(isDebug: Boolean) {
  inline def debug(message: String)(using Context): Unit =
    if (isDebug) {
      report.echo(message)
    }
}
