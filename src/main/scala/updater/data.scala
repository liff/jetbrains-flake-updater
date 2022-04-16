package updater

import java.time.LocalDate
import io.circe.Codec
import cats.Order
import cats.effect.Concurrent
import cats.data.{NonEmptyList, NonEmptySet}
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.Uri

enum Edition derives CanEqual { case Licensed, Community }
object Edition extends EnumCompanion[Edition]

enum Status derives CanEqual { case Release, Eap }
object Status extends EnumCompanion[Status]

enum Licensing derives CanEqual { case Release, Eap }
object Licensing extends EnumCompanion[Licensing]

enum Variant derives CanEqual { case Default, NoJbr }
object Variant extends EnumCompanion[Variant]

case class Build(
    number: String,
    version: String,
    releaseDate: Option[LocalDate],
    fullNumber: Option[String],
) derives CanEqual,
      Codec.AsObject {

  val fullNumbers: Option[List[Int]] = fullNumber.flatMap(_.split('.').toList.traverse(_.toIntOption))

  val numbers: Option[List[Int]] = number.split('.').toList.traverse(_.toIntOption)
}

object Build:
  given Order[Build] = Order.whenEqual(
    Order.by(_.fullNumbers),
    Order.whenEqual(Order.by(_.numbers), Order.whenEqual(Order.by(_.version), Order.by(_.releaseDate))),
  )

case class Channel(
    id: String,
    name: String,
    majorVersion: String,
    status: Status,
    licensing: Licensing,
    builds: NonEmptyList[Build],
) derives CanEqual,
      Codec.AsObject

case class Product(name: String, codes: NonEmptySet[String], channels: NonEmptyList[Channel])
    derives CanEqual,
      Codec.AsObject

opaque type Sha256 = String

object Sha256:
  given Order[Sha256] = cats.instances.string.catsKernelStdOrderForString

  given Encoder[Sha256] = Encoder.encodeString
  given Decoder[Sha256] = Decoder.decodeString

  given [F[_]: Concurrent]: EntityDecoder[F, Sha256] = EntityDecoder.text[F].map(_.takeWhile(_ != ' '))

case class Artifact(build: Build, downloadUri: Uri, sha256: Sha256) derives CanEqual, Codec.AsObject

object Artifact:
  given ord: Order[Artifact] =
    Order.whenEqual(Order.by(_.build), Order.whenEqual(Order.by(_.downloadUri), Order.by(_.sha256)))
  given Ordering[Artifact] = ord.toOrdering

type Packages = Map[String, Map[Edition, Map[Status, Map[Variant, List[Artifact]]]]]

object Packages:
  val empty: Packages = Map.empty

extension (packages: Packages)
  def findArtifact(
      product: String,
      edition: Edition,
      status: Status,
      variant: Variant,
      build: Build,
  ): Option[Artifact] =
    packages
      .get(product)
      .flatMap(_.get(edition))
      .flatMap(_.get(status))
      .flatMap(_.get(variant))
      .flatMap(_.find(_.build == build))
