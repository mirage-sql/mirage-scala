mirage-scala
============

The SQL Centric Database Access Library for Scala

## What's mirage-scala?

mirage-scala is Scala wrapper for [Mirage](https://github.com/takezoe/mirage).

It provides the dynamic SQL template language called [2waySQL](http://amateras.sourceforge.jp/site/mirage/2waysql.html). Directives is embedded as SQL comment, so the 2WaySQL template is also executable as raw SQL. It makes testability of SQL.

To use mirage-scala with sbt based project, please add following dependency into your `build.sbt`.

```scala
resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

libraryDependencies += "jp.sf.amateras.mirage" %% "mirage-scala" % "0.1.0"
```

This is a simple example to query using mirage-scala:

At first, define the DTO which is mapped to ResultList as case class. It's possible to specify `Option[T]` as property type for null-able properties.

```scala
// A class which mapped to ResultList
case class Book(
  bookId: IdentityPk[Int],
  bookName: String,
  author: String,
  price: Option[Int]
)
```

Execute SQL using `SqlManager`. mirage-scala provides dynamic SQL template called [2waySQL](http://amateras.sourceforge.jp/site/mirage/2waysql.html) in `Sql()`.

```scala
import jp.sf.amateras.mirage.scala._

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

You can also use the external SQL file using `SqlFile()`:

```scala
val result: List[Book] = sqlManager.getResultList[Book](
  SqlFile("META-INF/selectBooks.sql"),
  Map("author"->"Naoki Takezoe"))
```

In mirage-scala, it's also possible to use `Map[String, _]` as result class / parameter class instead of the case class.

See also [Mirage documentation](http://amateras.sourceforge.jp/site/mirage/welcome.html) to learn about usage of Mirage.

## SQL less update

mirage-scala also supports SQL less select / update using the entity class.

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

Also batch updating is available.

```scala
// batch inserting
sqlManager.insertBatch(book1, book2, book3);
sqlManager.insertBatch(books: _*);

// batch updating
sqlManager.updateBatch(book1, book2, book3);
sqlManager.updateBatch(books: _*);

// batch deleting
sqlManager.deleteBatch(book1, book2, book3);
sqlManager.deleteBatch(books: _*);
```

## Iteration search

To handle large data, mirage-scala provides iteration search.

```scala
var sum = 0
val result = sqlManager.iterate[Book, Int](
  Sql("SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE FROM BOOK"))
  { book =>
      sum = sum + book.price
      sum
  }
```

In this example, var `sum` is required to keep a total value outside of the callback function. You can remove it as following:

```scala
val result = sqlManager.iterate[Book, Int](
  Sql("SELECT BOOK_ID, BOOK_NAME, AUTHOR, PRICE FROM BOOK"), 0)
  { (book, sum) => sum + book.price }
```

The callback function is given a result of the previous callback invocation. And initial value is given at first. In this example, initial value is 0.
