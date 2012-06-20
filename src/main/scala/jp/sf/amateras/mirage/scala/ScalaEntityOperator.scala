package jp.sf.amateras.mirage.scala

import jp.sf.amateras.mirage.DefaultEntityOperator
import jp.sf.amateras.mirage.EntityOperator.PrimaryKeyInfo
import jp.sf.amateras.mirage.bean.PropertyDesc
import jp.sf.amateras.mirage.annotation.PrimaryKey
import jp.sf.amateras.mirage.util.MirageUtil
import jp.sf.amateras.mirage.naming.NameConverter

class ScalaEntityOperator extends DefaultEntityOperator {

  override def getPrimaryKeyInfo(clazz: Class[_],
      propertyDesc: PropertyDesc, nameConverter: NameConverter): PrimaryKeyInfo = {

    // by annotation
    val primaryKeyInfo = super.getPrimaryKeyInfo(clazz, propertyDesc, nameConverter)

    if(primaryKeyInfo != null){
      primaryKeyInfo

    } else {
      val propertyType = propertyDesc.getField.getType

	  if (propertyType == classOf[SequencePk[_]]){
	    val tableName = MirageUtil.getTableName(clazz, nameConverter)
	    val columnName = MirageUtil.getColumnName(this, clazz, propertyDesc, nameConverter)
        new PrimaryKeyInfo(PrimaryKey.GenerationType.SEQUENCE, tableName + "_" + columnName + "_SEQ")

      } else if (propertyType == classOf[IdentityPk[_]]){
        new PrimaryKeyInfo(PrimaryKey.GenerationType.IDENTITY)

      } else if(propertyType == classOf[ApplicationPk[_]] || propertyType == classOf[Pk[_]]){
        new PrimaryKeyInfo(PrimaryKey.GenerationType.APPLICATION)

      } else {
        null
      }
    }
  }

}