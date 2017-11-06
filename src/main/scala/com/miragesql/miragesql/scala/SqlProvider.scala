package com.miragesql.miragesql.scala

import com.miragesql.miragesql.scala.Utilities._
import com.miragesql.miragesql.util.IOUtil

/**
 * Provides an interface to give 2waySQL to SqlManager.
 */
trait SqlProvider {

  /**
   * Returns 2way SQL.
   */
  def getSql(): String

}

/**
 * SqlProvider for writing SQL inline.
 *
 * @constructor Constructor.
 * @param sql 2way SQL.
 */
case class Sql(sql: String) extends SqlProvider {

  def getSql(): String = sql

}

/**
 * SqlProvider for writing SQL in external file on classpath.
 *
 * @constructor Constructor.
 * @param path Path of a SQL file on classpath.
 */
case class SqlFile(path: String) extends SqlProvider {

  def getSql(): String = {
    val classLoader = Thread.currentThread.getContextClassLoader
    using(classLoader.getResourceAsStream(path)){ in =>
      if(in == null){
        throw new RuntimeException("'%s' does not exist.".format(path))
      }
      new String(IOUtil.readStream(in), "UTF-8")
    }

  }

}