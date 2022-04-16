package updater

import cats.data.{NonEmptySet, NonEmptyVector}
import io.circe.{KeyDecoder, KeyEncoder}

import scala.collection.immutable.SortedSet

abstract class EnumCompanion[A <: scala.reflect.Enum]:
  def valueOf(str: String): A
  def values: Array[A]

  given Ordering[A] = Ordering.by(_.ordinal)

  final lazy val valueSet: NonEmptySet[A] = NonEmptySet.fromSetUnsafe(SortedSet.from(values.toSet))

  final lazy val valueList: NonEmptyVector[A] = NonEmptyVector.fromVectorUnsafe(values.toVector)

  final def fromString(str: String): Option[A] =
    try Some(valueOf(str.capitalize))
    catch case _: IllegalArgumentException => None

  given KeyEncoder[A] = KeyEncoder[String].contramap(lowerCase)
  given KeyDecoder[A] = KeyDecoder.instance(fromString)
