package jp.sf.amateras.mirage.scala

import jp.sf.amateras.mirage.bean.PropertyWrapperImpl
import java.lang.reflect.Field

/**
 * This is a PropertyWrapper implementation for the property which has a type Pk[T].
 */
class PkFieldPropertyWrapper(name: String, field: Field) extends PropertyWrapperImpl(name, null, null, field) {

  override def get(instance: AnyRef): AnyRef = {
    val value = super.get(instance)
    value match {
      case Id(x: AnyRef) => x
      case Auto => null
    }
  }

  override def set(instance: AnyRef, value: AnyRef): Unit = {
    if(value == null){
      super.set(instance, Auto)
    } else {
      super.set(instance, Id(value))
    }
  }

  override def getType(): Class[_] = {
    Utilities.getWrappedType[Pk[_]](getField) match {
      case Some(x) => x
      case None => throw new RuntimeException("Failed to retrieve Pk type.")
    }
  }

}