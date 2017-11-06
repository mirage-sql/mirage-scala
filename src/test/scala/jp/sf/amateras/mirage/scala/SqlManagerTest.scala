package jp.sf.amateras.mirage.scala

import org.specs2.mutable._
import com.miragesql.miragesql.annotation._
import com.miragesql.miragesql.util.IOUtil

class SqlManagerTest extends Specification {
  sequential

  "getResultList()" should {
    "return correct results with JavaBean as a parameter" in new trees {
      val resultList = sqlManager.getResultList[Book](Sql("""
       SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE
       FROM BOOK
       /*IF author != null*/
         WHERE AUTHOR=/*author*/'Naoki Takezoe'
       /*END*/
      """), BookParam("Naoki Takezoe"))

      resultList.size mustEqual 2
      resultList(0).bookName mustEqual "Mirage in Action"
      resultList(1).bookName mustEqual "現場で使えるJavaライブラリ"
      resultList.foreach({book => println(book.bookName + ", " + book.price)})
    }

    "return correct results with Map as a parameter" in new trees {
      val resultList = sqlManager.getResultList[Map[String, _]](Sql("""
       SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE
       FROM BOOK
       /*IF bookName != null*/
         WHERE BOOK_NAME=/*bookName*/'Mirage in Action'
       /*END*/
      """), Map("bookName" -> "Mirage in Action"))

      resultList.size mustEqual 1
      resultList(0)("bookName").asInstanceOf[String] mustEqual "Mirage in Action"
      resultList.foreach({book => println(book("bookName") + ", " + book("price"))})
    }
  }

  "getCount()" should {
    "return a correct count"  in new trees {
      val count = sqlManager.getCount(Sql("SELECT BOOK_ID FROM BOOK"))
      count mustEqual 2
      println("count = " + count)
    }
  }

  "findEntity()" should {
    "return an entity that has a given primary key" in new trees {
      val clazz = classOf[Book]
      val book = sqlManager.findEntity[Book](1)
      book.get.bookName mustEqual "Mirage in Action"
    }
  }

  "iterate()" should {
    "invokes the callback method for each row" in new trees {
      val result = sqlManager
        .stream(Sql("SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE FROM BOOK"))
        .foldLeft[Book, Int](0) { case (book, sum) => sum + book.price.get }

      result mustEqual 8400
    }
  }

  "insertBatch()" should {
    "insert given entities and return a inserted row count" in new trees {
      val result = sqlManager.insertBatch(
          Book(Auto, "Programming Scala", "Dean Wampler, Alex Payne", Some(3669)),
          Book(Auto, "Programming in Scala", "Martin Odersky, Lex Spoon, Bill Venners", Some(4482)))
      result mustEqual 2

//      val book1 = sqlManager.getSingleResult(classOf[Book],
//          Sql("SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE FROM BOOK WHERE BOOK_NAME=/*bookName*/"),
//          Map("bookName" -> "Programming Scala"))
//
//      book1.get.author mustEqual "Dean Wampler, Alex Payne"
//      book1.get.price mustEqual 3669
//
//      val book2 = sqlManager.getSingleResult(classOf[Book],
//          Sql("SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE FROM BOOK WHERE BOOK_NAME=/*bookName*/"),
//          Map("bookName" -> "Programming in Scala"))
//
//      book2.get.author mustEqual "Martin Odersky, Lex Spoon, Bill Venners"
//      book2.get.price mustEqual 4482
    }

  }

  trait trees extends BeforeAfter {
    
    val SQL_PREFIX: String = "jp/sf/amateras/mirage/scala/";

    val session: Session = Session.get
    val sqlManager: SqlManager = session.sqlManager

    private def executeMultipleStatement(sqlPath: String) {
      val cl: ClassLoader = Thread.currentThread().getContextClassLoader()
      val bytes: Array[Byte] = IOUtil.readStream(cl.getResourceAsStream(sqlPath))
      val sql: String = new String(bytes, "UTF-8")
      sql.split(";").foreach { statement =>
        if(statement.trim().length() > 0){
          try {
            sqlManager.executeUpdate(Sql(statement))
          } catch {
            case e: Exception => e.printStackTrace()
          }
        }
      }
    }

    
    def before(){
      session.begin()
      executeMultipleStatement(SQL_PREFIX + "SqlManagerImplTest_setUp.sql")

      sqlManager.executeUpdate(Sql("""
      INSERT INTO BOOK (BOOK_ID, BOOK_NAME, AUTHOR, PRICE)
      VALUES (1, 'Mirage in Action', 'Naoki Takezoe', 4800)
      """))

      val entity = Book(Auto, "現場で使えるJavaライブラリ", "Naoki Takezoe", Some(3600))
      sqlManager.insertEntity(entity)
    }

    def after(){
      executeMultipleStatement(SQL_PREFIX + "SqlManagerImplTest_tearDown.sql")
      session.rollback
      session.release
    }
  }

}

/**
 * The entity class for the BOOK table.
 */
@Table(name="BOOK")
case class Book(
  bookId: IdentityPk[Long],
  bookName: String,
  author: String,
  price: Option[Int])

/**
 * The parameter class for searching by author.
 */
case class BookParam(val author: String)
