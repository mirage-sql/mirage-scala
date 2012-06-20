package jp.sf.amateras.mirage.scala
import org.specs.Specification
import jp.sf.amateras.mirage.bean.BeanDescFactory
import jp.sf.amateras.mirage.annotation.PrimaryKey.GenerationType
import jp.sf.amateras.mirage.naming.DefaultNameConverter

class ScalaEntityOperatorTest extends Specification {

  val nameConverter = new DefaultNameConverter
  val entityOperator = new ScalaEntityOperator

  BeanDescFactoryInitializer.initialize

  "getPrimaryKeyInfo()" should {
    "return GenerationType.APPLICATION for Pk" in {
      val clazz = classOf[TestEntity1]
      val beanDesc = BeanDescFactory.getBeanDesc(clazz)
      val primaryKeyInfo = entityOperator.getPrimaryKeyInfo(clazz, beanDesc.getPropertyDesc(0), nameConverter)

      primaryKeyInfo.generationType must be_==(GenerationType.APPLICATION)
      primaryKeyInfo.generator must beNull
    }
    "return GenerationType.IDENTITY for IdentityPk" in {
      val clazz = classOf[TestEntity2]
      val beanDesc = BeanDescFactory.getBeanDesc(clazz)
      val primaryKeyInfo = entityOperator.getPrimaryKeyInfo(clazz, beanDesc.getPropertyDesc(0), nameConverter)

      primaryKeyInfo.generationType must be_==(GenerationType.IDENTITY)
      primaryKeyInfo.generator must beNull
    }
    "return GenerationType.SEQUENCE for SequencePk" in {
      val clazz = classOf[TestEntity3]
      val beanDesc = BeanDescFactory.getBeanDesc(clazz)
      val primaryKeyInfo = entityOperator.getPrimaryKeyInfo(clazz, beanDesc.getPropertyDesc(0), nameConverter)

      primaryKeyInfo.generationType must be_==(GenerationType.SEQUENCE)
      primaryKeyInfo.generator must be_==("TEST_ENTITY3_ID_SEQ")
    }
    "return null for the property which is not the primary key" in {
      val clazz = classOf[TestEntity1]
      val beanDesc = BeanDescFactory.getBeanDesc(clazz)
      val primaryKeyInfo = entityOperator.getPrimaryKeyInfo(clazz, beanDesc.getPropertyDesc(1), nameConverter)

      primaryKeyInfo must beNull
    }
  }

  case class TestEntity1(id: Pk[Long], name: String)
  case class TestEntity2(id: IdentityPk[Long])
  case class TestEntity3(id: SequencePk[Long])

}