package scala3profile

import dotty.tools.dotc.transform.Inlining
import java.io.File

class ProfileInlining(
  override val outputFile: File,
  override val minTime: Long,
  override val categories: Seq[String]
) extends Inlining
    with ProfileBase
