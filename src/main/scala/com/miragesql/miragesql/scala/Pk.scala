package com.miragesql.miragesql.scala

import com.miragesql.miragesql.annotation.PrimaryKey

/**
 * This is the wrapper for the primary key property which is set value by the database side.
 *
 * {{{
 * case class Book(
 *   @(PrimaryKey @field)(generationType = IDENTITY)
 *   bookId: Pk[Long],
 *   bookName: String)
 * }}}
 *
 * This type corresponds to GenerationType.APPLICATION.
 */
abstract sealed trait Pk[+T] {
  def get(): T
}

/**
 * This type corresponds to GenerationType.APPLICATION.
 */
abstract sealed trait ApplicationPk[+T] extends Pk[T]

/**
 * This type corresponds to GenerationType.SEQUENCE.
 *
 * The sequence name which is used to generate the value for the column which has this type
 * must be <table_name>_<column_name>_SEQ.
 */
abstract sealed trait SequencePk[+T] extends Pk[T]

/**
 * This type corresponds to GenerationType.IDENTITY.
 */
abstract sealed trait IdentityPk[+T] extends Pk[T]

/**
 * Use this class to give the value to the property which is Pk[T].
 *
 * {{{
 * case class Book(
 *   @(PrimaryKey @field)(generationType = APPLICATION)
 *   bookId: Pk[Long],
 *   bookName: String)
 *
 * val book = Book(Id(1), "Mirage in Action")
 * sqlManager.insert(book)
 * }}}
 */
case class Id[+T](x: T) extends Pk[T] with SequencePk[T] with IdentityPk[T] {
  override def get(): T = x
}

/**
 * Use this object when you don't want to specify the value of the primary key
 * which is auto incremented at inserting the entity.
 *
 * {{{
 * case class Book(
 *   @(PrimaryKey @field)(generationType = IDENTITY)
 *   bookId: Pk[Long],
 *   bookName: String)
 *
 * val book = Book(Auto, "Mirage in Action")
 * sqlManager.insert(book)
 * }}}
 */
case object Auto extends Pk[Nothing] with SequencePk[Nothing] with IdentityPk[Nothing] {
  override def get(): Nothing = throw new NoSuchElementException("Auto.get")
}

