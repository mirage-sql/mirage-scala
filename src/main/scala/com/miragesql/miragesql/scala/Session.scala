package com.miragesql.miragesql.scala

/**
 * Session wrapper for Scala.
 * You can control transaction and get [[com.miragesql.miragesql.scala.SqlManager]] through this.
 *
 * @constructor Creates new instance of Session
 * @param session Raw Session of Mirage
 */
class Session(session: com.miragesql.miragesql.session.Session){

  /**
   * Begins the transaction.
   * You have to invoke this method before using SqlManager.
   */
  def begin(){
    session.begin()
  }

  /**
   * Commits the current transaction.
   */
  def commit(){
    session.commit()
  }

  /**
   * Marks the current transaction as rollback only.
   */
  def setRollbackOnly(){
    session.setRollbackOnly()
  }

  def isRollbackOnly: Boolean = session.isRollbackOnly

  /**
   * Returns an instance of SqlManager which is related this Session.
   */
  def sqlManager: SqlManager =
    SqlManager(session.getSqlManager.asInstanceOf[com.miragesql.miragesql.SqlManagerImpl])

  /**
   * Releases resources which related the current transaction.
   * You have to invoke this method after using SqlManager.
   */
  def release(){
    session.release()
  }

  /**
   * Rolls back the current transaction.
   */
  def rollback(){
    session.rollback()
  }

}

/**
 * Provides utility methods which related Session.
 *
 * == Getting Session ==
 *
 * You can get Session using Session#get.
 *
 * {{{
 * val session: Session = Session.get
 * val sqlManager:SqlManager session.sqlManager
 *
 * session.begin()
 *
 * try {
 *   ...
 *   session.commit()
 *
 * } catch {
 *   case ex: Exception => session.rollback()
 * }
 * }}}
 *
 * == Automatic Transaction Controlling ==
 *
 * You can use Session with automatic transaction controlling using Session#withTransaction.
 *
 * {{{
 * Session.withTransaction { session =>
 *   val sqlManager:SqlManager session.sqlManager
 *   ...
 * }
 * }}}
 */
object Session {

  /**
   * Creates new instance of Session.
   */
  def get: Session =  new Session(com.miragesql.miragesql.session.SessionFactory.getSession)

  /**
   * Runs the given function which uses Session with automatic transaction controlling.
   *
   * If the function throws any Exception or Session#setRollbackOnly has been invoked,
   * Session will be rolled back. Otherwise Session will be committed.
   */
  def withTransaction[R](func: (Session) => R): R = {
    val session = Session.get
    session.begin()
    try {
      val result = func(session)

      if(session.isRollbackOnly){
        session.rollback()
      } else {
        session.commit()
      }
      return result
    } catch {
      case ex: Exception => {
        session.rollback()
        throw ex
      }
    } finally {
      session.release()
    }

  }

}
