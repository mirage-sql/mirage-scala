package jp.sf.amateras.mirage.scala

import jp.sf.amateras.mirage.bean.DefaultPropertyExtractor
import jp.sf.amateras.mirage.bean.PropertyWrapper
import jp.sf.amateras.mirage.bean.PropertyExtractor.PropertyInfo
import jp.sf.amateras.mirage.scala.Utilities._
import jp.sf.amateras.mirage.bean.PropertyWrapperImpl
import java.lang.reflect.Modifier
import java.lang.reflect.Field

/**
 * PropertyExtractor implementation for Scala.
 *
 * This implementation recognizes public fields of Scala classes as a property.
 */
class ScalaPropertyExtractor extends DefaultPropertyExtractor {

  override def extractProperties(clazz: java.lang.Class[_]): java.util.Map[String, PropertyWrapper] = {
    val map = super.extractProperties(clazz)
    extract(clazz, map)
    map
  }

  private def extract(clazz: Class[_], map: java.util.Map[String, PropertyWrapper]): Unit = {
    if(clazz != null) {
      for (field <- clazz.getDeclaredFields) {
        if (!Modifier.isPublic(field.getModifiers)) {
          val propertyName = field.getName
          withoutException {
            val field = clazz.getDeclaredField(propertyName)
            field.setAccessible(true)
            if (field.getType == classOf[Option[_]]) {
              map.put(propertyName, new OptionFieldPropertyWrapper(propertyName, field))
            } else if (field.getType == classOf[Pk[_]]) {
              map.put(propertyName, new PkFieldPropertyWrapper(propertyName, field))
            } else {
              map.put(propertyName, new PropertyWrapperImpl(propertyName, null, null, field))
            }
          }
        }
      }
      for (field <- clazz.getDeclaredFields) {
        if (!Modifier.isPublic(field.getModifiers)) {
          val propertyName = field.getName
          withoutException {
            val method = clazz.getMethod(propertyName)
            if (map.containsKey(propertyName)) {
              map.get(propertyName).setGetterMethod(method)
            } else {
              map.put(propertyName, new PropertyWrapperImpl(propertyName, method, null, getField(clazz, propertyName)))
            }
          }
          withoutException {
            val method = clazz.getMethod(propertyName + "_$eq", field.getType)
            if (map.containsKey(propertyName)) {
              map.get(propertyName).setSetterMethod(method)
            } else {
              map.put(propertyName, new PropertyWrapperImpl(propertyName, null, method, getField(clazz, propertyName)))
            }
          }
        }
      }
      extract(clazz.getSuperclass, map)
    }
  }

  private def getField(clazz: Class[_], name: String): Field = withoutExceptionOrNull { clazz.getDeclaredField(name) }

}