package example

import scala.quoted.*

object A1 {
  inline def foo: Unit = ${ fooImpl }

  def fooImpl(using Quotes): Expr[Unit] = {
    Thread.sleep(1000)
    '{ () }
  }
}
