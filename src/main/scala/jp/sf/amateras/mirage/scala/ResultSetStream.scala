package jp.sf.amateras.mirage.scala

import com.miragesql.miragesql.{IterationCallback, SqlExecutor}

class ResultSetStream(sqlExecutor: SqlExecutor, sql: SqlProvider, param: AnyRef) {

  def foreach[T](op: (T) => Unit)(implicit m: scala.reflect.Manifest[T]): Unit = {
    val clazz = m.runtimeClass.asInstanceOf[Class[T]]
    val (prepareSql, bindVariables) = Utilities.parseSql(sql, param)

    sqlExecutor.iterate(clazz, new IterationCallbackAdapter(op), prepareSql, bindVariables)
  }

  def foldLeft[T, R](z: R)(op: (T, R) => R)(implicit m: scala.reflect.Manifest[T]): R = {
    var result = z
    foreach { t: T =>
      result = op(t, result)
    }(m)
    result
  }

  /**
   * Adapter for that callback function that would be given to iterate().
   */
  private class IterationCallbackAdapter[T, R](val callback: (T) => R) extends IterationCallback[T, R] {
    def iterate(entity: T): R = callback(entity)
  }

}
