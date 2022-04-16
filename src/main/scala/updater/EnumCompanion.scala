package updater

import cats.data.{NonEmptySet, NonEmptyVector}
import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import cats.{Order, Show}
import org.typelevel.ci.CIString

import scala.collection.immutable.SortedSet

abstract class EnumCompanion[A <: scala.reflect.Enum]:
  def valueOf(str: String): A
  def values: Array[A]

  given ord: Order[A] = Order.by(_.ordinal)
  given Ordering[A]   = ord.toOrdering
  given Show[A]       = Show.fromToString

  final lazy val valueSet: NonEmptySet[A] = NonEmptySet.fromSetUnsafe(SortedSet.from(values.toSet))

  final lazy val valueList: NonEmptyVector[A] = NonEmptyVector.fromVectorUnsafe(values.toVector)

  final def fromString(str: String): Option[A] =
    valueSet.find(value => CIString(value.toString) === CIString(str))

  given KeyEncoder[A] = KeyEncoder[String].contramap(lowerCase)
  given KeyDecoder[A] = KeyDecoder.instance(str => fromString(str))

  given Encoder[A] = Encoder[String].contramap(lowerCase)
  given Decoder[A] = Decoder[String].emap { str =>
    fromString(str).toRight(s"Unrecognized enumeration value $str")
  }
