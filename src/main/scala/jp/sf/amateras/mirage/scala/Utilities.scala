package jp.sf.amateras.mirage.scala
import scala.tools.scalap.scalax.rules.scalasig.ScalaSigParser
import java.lang.reflect.Member
import java.io.ByteArrayOutputStream
import scala.tools.scalap.scalax.rules.scalasig.ScalaSigPrinter
import java.io.PrintStream
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
      case ex: Exception => null
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
      // Print classes
      val baos = new ByteArrayOutputStream
      val stream = new PrintStream(baos)
      val printer = new ScalaSigPrinter(stream, true)
      for (c <- syms) {
        if (c.path == member.getDeclaringClass().getName())
          printer.printSymbol(c)
      }
      val fullSig = baos.toString
      val clazz = m.erasure.asInstanceOf[Class[T]]
      val matcher = """\s%s : %s\[scala\.(\w+)\]?""".format(member.getName, clazz.getName).r.pattern.matcher(fullSig)
      if (matcher.find) {
        matcher.group(1) match {
          case "Int"     => Some(classOf[scala.Int])
          case "Short"   => Some(classOf[scala.Short])
          case "Long"    => Some(classOf[scala.Long])
          case "Double"  => Some(classOf[scala.Double])
          case "Float"   => Some(classOf[scala.Float])
          case "Boolean" => Some(classOf[scala.Boolean])
          case "Byte"    => Some(classOf[scala.Byte])
          case "Char"    => Some(classOf[scala.Char])
          case _ => None //Unknown scala primitive type?
        }
      } else
        None //Pattern was not found anywhere in the signature
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

}