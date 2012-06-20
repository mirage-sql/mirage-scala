package jp.sf.amateras.mirage.scala

import collection.JavaConversions._
import jp.sf.amateras.mirage.parser.{SqlContext, Node, SqlParserImpl}
import jp.sf.amateras.mirage.{IterationCallback, SqlManagerImpl, SqlExecutor}

/**
 * SqlManager wrapper for Scala.
 */
class SqlManager private (sqlManager: jp.sf.amateras.mirage.SqlManagerImpl) {

  private lazy val sqlExecutor: SqlExecutor = Utilities.getField(sqlManager, "sqlExecutor")

  /**
   * Returns a single result.
   *
   * @param clazz the type of result
   * @param sql the sql
   * @param param the parameter object
   * @return Some(result) or None
   */
  def getSingleResult[T](sql: SqlProvider, param: AnyRef = null)(implicit m: scala.reflect.Manifest[T]): Option[T] = {
    val clazz = m.erasure.asInstanceOf[Class[T]]
    val node: Node = new SqlParserImpl(sql.getSql()).parse()
    val context: SqlContext = prepareSqlContext(convertParam(param))
    node.accept(context)

    Option(sqlExecutor.getSingleResult(clazz, context.getSql, context.getBindVariables))
  }

  /**
   * Returns a list of results.
   *
   * @param clazz the type of result
   * @param sql the sql
   * @param param the parameter object
   * @return Some(result) or None
   */
  def getResultList[T](sql: SqlProvider, param: AnyRef = null)(implicit m: scala.reflect.Manifest[T]): List[T] = {
    val clazz = m.erasure.asInstanceOf[Class[T]]
    val node: Node = new SqlParserImpl(sql.getSql()).parse()
    val context: SqlContext = prepareSqlContext(convertParam(param))
    node.accept(context)

    if(clazz == classOf[Map[String, _]]){
      // convert java.util.Map to scala.Map
      sqlExecutor.getResultList(classOf[java.util.Map[String, _]], context.getSql,
        context.getBindVariables).toList.map { entry => entry.toMap }.asInstanceOf[List[T]]
    } else {
      sqlExecutor.getResultList(clazz, context.getSql, context.getBindVariables).toList
    }
  }

  def iterate[T, R](sql: SqlProvider, param: AnyRef = null)(callback: (T) => R)(implicit m: scala.reflect.Manifest[T]): R = {
    val clazz = m.erasure.asInstanceOf[Class[T]]
    val node: Node = new SqlParserImpl(sql.getSql()).parse()
    val context: SqlContext = prepareSqlContext(convertParam(param))
    node.accept(context)

    sqlExecutor.iterate(clazz, new IterationCallbackAdapter(callback), context.getSql, context.getBindVariables)
  }

  def iterate[T, R](sql: SqlProvider, context: R)(callback: (T, R) => R)(implicit m: scala.reflect.Manifest[T]): R = iterate(sql, null, context)(callback)(m)

  def iterate[T, R](sql: SqlProvider, param: AnyRef, context: R)(callback: (T, R) => R)(implicit m: scala.reflect.Manifest[T]): R = {
    var result = context
    iterate(sql, param){ t: T =>
      result = callback(t, result)
      result
    }(m)
  }

  /**
   * Executes the updating SQL.
   *
   * @param sql the sql
   * @param param the parameter object
   * @return updated row count
   */
  def executeUpdate(sql: SqlProvider, param: AnyRef = null): Int = {
    val node: Node = new SqlParserImpl(sql.getSql()).parse()
    val context: SqlContext = prepareSqlContext(convertParam(param))
    node.accept(context)

    sqlExecutor.executeUpdateSql(context.getSql, context.getBindVariables, null)
  }

  /**
   * Returns the row count of the query.
   *
   * @param sql the sql
   * @param param the parameter object
   * @return the row count of the executed SQL
   */
  def getCount(sql: SqlProvider, param: AnyRef = null): Int = {
    val node: Node = new SqlParserImpl(sql.getSql()).parse()
    val context: SqlContext = prepareSqlContext(convertParam(param))
    node.accept(context)

    val countSql: String = sqlManager.getDialect.getCountSql(context.getSql);
    sqlExecutor.getSingleResult(classOf[java.lang.Integer], countSql, context.getBindVariables).intValue
  }

  /**
   * Finds the entity by the given primary key.
   *
   * @param clazz the type of entity
   * @param id primary keys
   * @return the entity. If the entity which corresponds to the given primary key is not found, this method returns None.
   */
  def findEntity[T](id: Any*)(implicit m: scala.reflect.Manifest[T]): Option[T] = {
    val clazz = m.erasure.asInstanceOf[Class[T]]
    Option(sqlManager.findEntity(clazz, id.map {_.asInstanceOf[AnyRef]}: _*))
  }

  /**
   * Inserts the given entity.
   *
   * @param entity the entity to insert
   * @return updated row count
   */
  def insertEntity(entity: AnyRef): Int = sqlManager.insertEntity(entity)

  /**
   * Inserts given entities with batch mode.
   *
   * @param entities entities to insert
   * @return updated row count
   */
  def insertBatch(entities: AnyRef*): Int = sqlManager.insertBatch(entities: _*)

  /**
   * Updates the given entity.
   *
   * @param entity the entity to update
   * @return updated row count
   */
  def updateEntity(entity: AnyRef): Int = sqlManager.updateEntity(entity)

  /**
   * Updates given entities with batch mode.
   *
   * @param entities entities to update
   * @return updated row count
   */
  def updateBatch(entities: AnyRef*): Int = sqlManager.updateBatch(entities: _*)

  /**
   * Deletes the given entity.
   *
   * @param entity the entity to delete
   * @return updated row count
   */
  def deleteEntity(entity: AnyRef): Int = sqlManager.deleteEntity(entity)

  /**
   * Deletes given entities with batch mode.
   *
   * @param entities entities to insert
   * @return updated row count
   */
  def deleteBatch(entities: AnyRef*): Int = sqlManager.deleteBatch(entities: _*)

  /**
   * Converts to java.util.Map if param is scala.Map.
   */
  private def convertParam(param: AnyRef): AnyRef = param match {
    case map: Map[_, _] => (map:java.util.Map[_, _])
    case params => params
  }

  private def prepareSqlContext(param: AnyRef): SqlContext =
    Utilities.invokeMethod(sqlManager, "prepareSqlContext", Array(classOf[Object]), Array(param))

  /**
   * Adapter for that callback function that would be given to iterate().
   */
  private class IterationCallbackAdapter[T, R](val callback: (T) => R) extends IterationCallback[T, R] {
    def iterate(entity: T): R = callback(entity)
  }

}

object SqlManager {
  BeanDescFactoryInitializer.initialize() //It is called only once.
  def apply(sqlManager: SqlManagerImpl): SqlManager = {
    sqlManager.setEntityOperator(new ScalaEntityOperator())
    new SqlManager(sqlManager)
  }
}