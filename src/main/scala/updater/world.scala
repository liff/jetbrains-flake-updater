package updater

import fs2.io.file.Files
import fs2.io.file.Path
import io.circe.fs2.*
import io.circe.{Decoder, Encoder}
import cats.effect.Async
import fs2.Stream
import fs2.text.utf8
import cats.Show
import java.util.Locale

def readJsonFile[F[_]: Async, A: Decoder](path: Path): F[A] =
  Files[F].readAll(path).through(byteStreamParser).through(decoder[F, A]).compile.lastOrError

def writeJsonFile[F[_]: Async, A: Encoder](path: Path, a: A): F[Unit] =
  Stream.emit(a.asJson.spaces4).through(utf8.encode).through(Files[F].writeAll(path)).compile.drain

def lowerCase[A: Show](a: A): String = a.show.toLowerCase(Locale.ROOT).nn
