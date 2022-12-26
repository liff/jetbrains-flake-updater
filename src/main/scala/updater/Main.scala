package updater

import cats.data.OptionT
import java.nio.file.NoSuchFileException
import cats.effect.*
import io.circe.DecodingFailure
import com.monovore.decline.*
import com.monovore.decline.effect.*
import fs2.Stream
import fs2.io.file.Files
import fs2.io.file.Path
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.`User-Agent`
import org.http4s.ProductId
import org.http4s.client.dsl.io.*
import org.http4s.Method.GET
import formats.given

object Main
    extends CommandIOApp(
      name = BuildInfo.name,
      header = BuildInfo.description,
      helpFlag = true,
      version = BuildInfo.version,
    ):

  private object O:
    val debug = Opts.flag("debug", help = "Enable debug logging.", short = "d").orFalse

    val stateFile = Opts.argument[Path](metavar = "STATE-FILE")

  override val main: Opts[IO[ExitCode]] =
    (O.debug, O.stateFile).mapN { (debug, stateFile) =>
      httpClientResource[IO](logging = debug).use { http =>
        for
          current <- readJsonFileOrEmpty[IO, Packages](stateFile)

          products <- http.expect[List[Product]](config.uris.updates)

          updated <-
            def resolve(product: String, edition: Edition, status: Status, variant: Variant, build: Build)
                : IO[Option[Artifact]] =
              val known = current.findArtifact(product, edition, status, variant, build).toOptionT[IO]

              val resolved = (for
                downloadUri <- config.downloadUriFor(product, edition, status, variant, build).toOptionT[IO]
                sha256      <- OptionT(http.expectOption[Sha256](GET(downloadUri % "sha256")))
              yield Artifact(build, downloadUri, sha256))

              known.orElse(resolved).value

            products.collected { case Product(product, _, channels) =>
              product -*> Edition.valueList.parCollected { edition =>
                edition -*> channels.parCollected { case Channel(_, _, _, status, _, builds) =>
                  status -*> Variant.valueList.parCollected { variant =>
                    variant -*> builds
                      .parTraverse(resolve(product, edition, status, variant, _))
                      .map(_.toList.unite.sorted.reverse)
                  }
                }
              }
            }

          _ <-
            val currentList = current.explode.sorted
            val updatedList = updated.explode.sorted
            def pretty(entry: (String, Edition, Status, Variant, Artifact)) =
              val (product, edition, status, variant, artifact) = entry
              s"$product $edition $status ($variant) ${artifact.build.version} [${artifact.build.number}]"

            import cats.syntax.all.*
            val newEntries =
              updatedList.filterNot(currentList.contains).map(pretty).mkString_("New:\n  - ", "\n  - ", "\n")
            val deleted =
              currentList.filterNot(updatedList.contains).map(pretty).mkString_("Deleted:\n  - ", "\n  - ", "\n")

            IO.println(newEntries ++ deleted)

          _ <- updated.writeTo[IO](stateFile)

        yield ExitCode.Success
      }
    }
