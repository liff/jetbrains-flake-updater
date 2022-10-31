package updater

import cats.{Eq, Foldable, Functor, Monad, Monoid, Parallel, Traverse}
import org.http4s.Uri
import scala.collection.immutable.SortedMap
import cats.Order

export cats.syntax.all.*
export io.circe.syntax.EncoderOps
export io.circe.syntax.KeyOps
export org.http4s.syntax.all.*

extension [F[_]: Foldable, A: Order: Ordering, B: Monoid: Eq](xs: F[(A, B)])
  def smush: SortedMap[A, B] =
    xs.filter_(!_._2.isEmpty).map((k, v) => SortedMap(k -> v)).combineAll

extension [T[_]: Traverse, A](xs: T[A])
  def parCollected[M[_]: Monad: Parallel, B: Order: Ordering, C: Monoid: Eq](f: A => M[(B, C)]): M[SortedMap[B, C]] =
    xs.parTraverse(f).map(_.smush)

  def collected[M[_]: Monad, B: Order: Ordering, C: Monoid: Eq](f: A => M[(B, C)]): M[SortedMap[B, C]] =
    xs.traverse(f).map(_.smush)

extension [A](a: A) def -*>[F[_]: Functor, B](fb: F[B]): F[(A, B)] = fb.map(a -> _)

extension (path: Uri.Path)
  def withFileExtension(ext: String): Uri.Path =
    if path.endsWithSlash || path.isEmpty then path
    else
      val init = path.segments.dropRight(1)
      path.segments.lastOption
        .map { last =>
          Uri.Path(
            segments = init :+ Uri.Path.Segment(last.encoded ++ s".$ext"),
            absolute = path.absolute,
            endsWithSlash = path.endsWithSlash,
          )
        }
        .getOrElse(path)

extension (uri: Uri) def %(ext: String): Uri = uri.withPath(uri.path.withFileExtension(ext))
