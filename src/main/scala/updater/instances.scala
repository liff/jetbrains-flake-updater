package updater

import com.monovore.decline.*
import com.monovore.decline.effect.*
import fs2.io.file.Path
import cats.{Order, Show}
import io.circe.{KeyDecoder, KeyEncoder}

export org.typelevel.cats.time.instances.all.*

export org.http4s.circe.{decodeUri, encodeUri}

given Argument[Path] = Argument.readPath.map(Path.fromNioPath)
