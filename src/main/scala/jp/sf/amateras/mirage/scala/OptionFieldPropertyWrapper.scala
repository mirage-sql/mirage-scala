package jp.sf.amateras.mirage.scala

import com.miragesql.miragesql.bean.PropertyWrapperImpl
import java.lang.reflect._

/**
 * This is a PropertyWrapper implementation for the property which has a type Option[T].
 */
class OptionFieldPropertyWrapper(name: String, field: Field) extends PropertyWrapperImpl(name, null, null, field) {

  override def get(instance: AnyRef): AnyRef = {
    val value = super.get(instance)
    value match {
      case Some(x: AnyRef) => x
      case None => null
    }
  }

  override def set(instance: AnyRef, value: AnyRef): Unit = {
    if(value == null){
      super.set(instance, None)
    } else {
      super.set(instance, Some(value))
    }
  }

  override def getType(): Class[_] = {
    Utilities.getWrappedType[Option[_]](getField) match {
      case Some(x) => x
      case None => throw new RuntimeException("Failed to retrieve Option type.")
    }
  }

}