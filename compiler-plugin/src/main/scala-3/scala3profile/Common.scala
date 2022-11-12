package scala3profile

import java.time.Clock
import java.time.Instant
import java.util.concurrent.TimeUnit

object Common {
  def microTime(): Long = TimeUnit.NANOSECONDS.toMicros(System.nanoTime())

  def jsonLine(name: String, category: List[String], time: Time, threadId: Long): String = {
    val c = category.mkString(",")
    s"""{"name":"${name}","cat":"${c}","ph":"X","ts":${time.start},"dur":${time.duration},"pid":0,"tid":${threadId}}"""
  }

}
