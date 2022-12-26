package updater

import fs2.io.file.Files
import fs2.io.file.Path
import io.circe.fs2.*
import io.circe.{Decoder, Encoder}
import cats.effect.{Sync, Async}
import fs2.Stream
import fs2.text.utf8
import cats.Show
import cats.kernel.Monoid
import java.nio.file.NoSuchFileException
import java.util.Locale

def readJsonFile[F[_]: Async, A: Decoder](path: Path): F[A] =
  Files[F].readAll(path).through(byteStreamParser).through(decoder[F, A]).compile.lastOrError

def readJsonFileOrEmpty[F[_]: Async, A: Decoder: Monoid](path: Path): F[A] =
  readJsonFile(path).recover { case _: NoSuchFileException => Monoid[A].empty }

extension [A: Encoder](a: A)
  def writeTo[F[_]: Files: Sync](path: Path): F[Unit] =
    Stream.emit(a.asJson.spaces4).through(utf8.encode).through(Files[F].writeAll(path)).compile.drain

def lowerCase[A: Show](a: A): String = a.show.toLowerCase(Locale.ROOT).nn
