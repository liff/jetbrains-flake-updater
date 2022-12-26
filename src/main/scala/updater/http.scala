package updater

import cats.{MonadError, MonadThrow}
import cats.syntax.all.*
import org.http4s.client.middleware.{FollowRedirect, Logger}
import cats.effect.{Async, Concurrent, ExitCode, IO, IOApp, MonadCancelThrow, Sync}
import org.http4s.ember.client.EmberClientBuilder
import cats.effect.std.Console
import org.http4s.Status.{NotFound, Successful}
import org.http4s.headers.`User-Agent`
import org.http4s.{EntityDecoder, ProductId, Request, Uri}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.Method.GET
import fs2.text.base64
import fs2.hash.sha256
import org.http4s.Status

def httpClientResource[F[_]: Async: Console](logging: Boolean) =
  EmberClientBuilder
    .default[F]
    .withUserAgent(userAgent)
//    .withHttp2
    .build
    .map { it =>
      FollowRedirect(3)(if logging then enableLogging(it) else it)
    }

private def enableLogging[F[_]: Async: Console](client: Client[F]) =
  Logger.colored[F](
    logHeaders = true,
    logBody = true,
    redactHeadersWhen = _ => false,
    logAction = Some(Console[F].errorln),
  )(client)

private val userAgent = `User-Agent`(ProductId(BuildInfo.name, Some(BuildInfo.version)))

given CanEqual[Status, Status] = CanEqual.derived

extension [E, F[_], A](fa: F[A])(using F: MonadError[F, E])
  def swallowErrorIf(cond: PartialFunction[E, Boolean]): F[Option[A]] =
    fa.redeemWith(
      recover = e => if (cond.applyOrElse(e, _ => false)) then None.pure else e.raiseError,
      bind = _.some.pure,
    )

extension [F[_]: MonadThrow, A](fa: F[A])
  def notFoundIsNone: F[Option[A]] = fa.swallowErrorIf { case UnexpectedStatus(NotFound, _, _) => true }
