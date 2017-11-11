Mirage-SQL Scala [![Build Status](https://travis-ci.org/mirage-sql/mirage-scala.svg?branch=master)](https://travis-ci.org/mirage-sql/mirage-scala) [![Join the chat at https://gitter.im/mirage-sql/mirage-sql](https://badges.gitter.im/mirage-sql/mirage-sql.svg)](https://gitter.im/mirage-sql/mirage-sql?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
================

The SQL-Centric Database Access Library for Scala.

## Introduction

**`Mirage-SQL Scala`** is wrapper written in Scala for the **[Mirage SQL](https://github.com/mirage-sql/mirage/)** framework.

Among other features, it provides a dynamic SQL-Template language called: [2WaySQL](https://github.com/mirage-sql/mirage/wiki/2WaySQL). 

Template directives are embedded as **SQL comments**, so that the 2WaySQL template is also executable as raw a SQL at the same time. This makes `SQL` files both dynamic and simply testable at the same time.

To use **`Mirage-SQL Scala`** with an SBT based project, just add following dependency to your `build.sbt`.

```scala
libraryDependencies += "com.miragesql" %% "miragesql-scala" % "2.0.0"
```

## Links:
 - A more **detailed documentation** will provided in the central [Wiki](https://github.com/mirage-sql/mirage/wiki).
 - **User Support** in English is provided in the [Gitter Chatroom](https://gitter.im/mirage-sql/mirage-sql).
 - If you find any **bugs or issues**, please report them in the [GitHub Issue Tracker](https://github.com/mirage-sql/mirage-scala/issues).


## Usage

#### A. 2WaySQL Dynamic Template

This is a simple example to query using **`Mirage-SQL Scala`**:

First, define the DTO which is mapped to ResultList as case class. It's possible to specify `Option[T]` as property type for nullable properties.

```scala
// A class which mapped to ResultList
case class Book(
  bookId: IdentityPk[Int],
  bookName: String,
  author: String,
  price: Option[Int]
)
```

Execute the `SQL` using `SqlManager`. mirage-scala provides dynamic SQL template called [2waySQL](https://github.com/mirage-sql/mirage/wiki/2WaySQL) in `Sql()`.

```scala
import com.miragesql.miragesql.scala._

Session.withTransaction { session =>
  val sqlManager: SqlManager = session.sqlManager

  val books: List[Book] = sqlManager.getResultList[Book](
    Sql("""
      SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE
      FROM BOOK
      /*IF author!=null*/
        WHERE AUTHOR = /*author*/
      /*END*/
    """), Map("author"->"Naoki Takezoe"))

  books.foreach { book =>
    println("bookId: " + book.bookId)
    println("bookName: " + book.bookName)
    println("author: " + book.author)
    println("price: " + book.price)
    println("--")
  }
}
```

You can also use an external SQL file using `SqlFile()`:

```scala
val result: List[Book] = sqlManager.getResultList[Book](
  SqlFile("META-INF/selectBooks.sql"),
  Map("author"->"Naoki Takezoe"))
```

With **`Mirage-SQL Scala`**, it's also possible to use `Map[String, _]` as result class / parameter class instead of the case class.

See also the [Mirage SQL Documentation](https://github.com/mirage-sql/mirage/wiki/Introduction) to learn more about it's usage.



#### B. SQL-less Updates

**`Mirage-SQL Scala`** also supports SQL-less select / update using an entity class.

If the primary key is set at the server-side, for example, it's auto incremented, You have to specify `Auto` for the primary key property.

```scala
val book: Book = Book(
  Auto,
  "Mirage in Action",
  "Naoki Takezoe",
  Some(20)
)

sqlManager.insertEntity(book);
```

If the primary key must be set by the application, you can use `Id(value)` to set the value to the primary key property.

```scala
val book: Book = Book(
  Id(1),
  "Mirage in Action",
  "Naoki Takezoe",
  Some(20)
)

sqlManager.insertEntity(book);
```

Batch updating is also available.

```scala
// batch inserting
sqlManager.insertBatch(book1, book2, book3)
sqlManager.insertBatch(books: _*)

// batch updating
sqlManager.updateBatch(book1, book2, book3)
sqlManager.updateBatch(books: _*)

// batch deleting
sqlManager.deleteBatch(book1, book2, book3)
sqlManager.deleteBatch(books: _*)
```

#### C. ResultSet Streams

To handle large data, create streams by `stream()` method and process each records by `foreach()` method.

```scala
sqlManager
  .stream(Sql("SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE FROM BOOK"))
  .foreach[Book] { book =>
    println(book)
  }
```

If you would like to aggregate streaming values, you can use `foldLeft` method instead:

```scala
val sum = sqlManager
  .stream(Sql("SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE FROM BOOK"))
  .foldLeft[Book, Int](0){ case (book, i) =>
    i + book.price
  }
```
