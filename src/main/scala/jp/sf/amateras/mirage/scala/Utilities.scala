package jp.sf.amateras.mirage.scala

import scala.tools.scalap.scalax.rules.scalasig._
import java.lang.reflect.{Field, Member}
import jp.sf.amateras.mirage.parser.SqlParserImpl
import jp.sf.amateras.mirage.util.MirageUtil
import collection.JavaConversions._

/**
 * Provides utility methods used by mirage-scala.
 */
object Utilities {

  /**
   * Runs the fiven function with the Closeable.
   * The Closeable is closed certainly after the execution of the function.
   */
  def using[A, B <: {def close(): Unit}] (closeable: B) (f: B => A): A =
    try {
      f(closeable)
    } finally {
      if(closeable != null){
        withoutException { closeable.close() }
      }
    }

  /**
   * Runs the given function without any exceptions and returns the return value of the function.
   * If any exceptions are occured, this method returns null.
   */
  def withoutExceptionOrNull[A, A1 >: A](func: => A)(implicit ev: Null <:< A1): A1 = {
    try {
      func
    } catch {
      case ex: Exception => null.asInstanceOf[A1]
    }
  }

  /**
   * Runs the given function without any exceptions and returns the return value of the function as Some(value).
   * If any exceptions are occured, this method returns None.
   */
  def withoutException[T](func: => T): Option[T] = {
    try {
      Some(func)
    } catch {
      case ex: Exception => None
    }
  }

  /**
   * Returns the specified field value by reflection.
   */
  def getField[T](target: AnyRef, fieldName: String): T = {
    val field = target.getClass().getDeclaredField(fieldName)
    field.setAccessible(true)
    field.get(target).asInstanceOf[T]
  }

  /**
   * Invokes the specified method by reflection.
   */
  def invokeMethod[T](target: AnyRef, methodName: String, types: Array[Class[_]], params: Array[AnyRef]): T = {
    val method = target.getClass().getDeclaredMethod(methodName, types:_*)
    method.setAccessible(true)
    method.invoke(target, params:_*).asInstanceOf[T]
  }

  def detectScalapOnClasspath(): Boolean = {
    try {
      Class.forName("scala.tools.scalap.scalax.rules.scalasig.ByteCode")
      true
    } catch {
      case cnfe : ClassNotFoundException => false
    }
  }

  def getWrappedType[T](member: Member)(implicit m: Manifest[T]): Option[Class[_]] = {
    val scalaSigOption = ScalaSigParser.parse(member.getDeclaringClass())

    scalaSigOption flatMap { scalaSig =>
      val syms = scalaSig.topLevelClasses
      val _type = syms.collectFirst {
        case c if (c.path == member.getDeclaringClass().getName) =>
          member match {
            case _: Field => findField(c, member.getName).map { f =>
              findArgTypeForField(f, 0)
            }
            case _: Method => throw new RuntimeException("Method type is not supported in mirage-scala.")
          }
      }
      _type.flatten
    }
  }

  /**
   * Returns a tuple of the SQL statement and bind variable.
   */
  def parseSql[A](sql: SqlProvider, param: A) = {
    val node = new SqlParserImpl(sql.getSql()).parse()
    val context = MirageUtil.getSqlContext {
      // Converts to java.util.Map if param is scala.Map
      param match {
        case map: Map[_, _] => (map:java.util.Map[_, _])
        case params => params
      }
    }
    node.accept(context)
    (context.getSql, context.getBindVariables)
  }

  private def findField(c: ClassSymbol, name: String): Option[MethodSymbol] =
    (c.children collect { case m: MethodSymbol if m.name == name => m }).headOption

  private def findArgTypeForField(s: MethodSymbol, typeArgIdx: Int): Class[_] = {
    val t = s.infoType match {
      case NullaryMethodType(TypeRefType(_, _, args)) => args(typeArgIdx)
    }

    toClass(t match {
      case TypeRefType(_, symbol, _)   => symbol
      case x => throw new Exception("Unexpected type info " + x)
    })
  }

  private def toClass(s: Symbol) = s.path match {
    case "scala.Short"         => classOf[Short]
    case "scala.Int"           => classOf[Int]
    case "scala.Long"          => classOf[Long]
    case "scala.Boolean"       => classOf[Boolean]
    case "scala.Float"         => classOf[Float]
    case "scala.Double"        => classOf[Double]
    case "scala.Byte"          => classOf[Byte]
    case "scala.Predef.String" => classOf[String]
    case x                     => Class.forName(x)
  }

}