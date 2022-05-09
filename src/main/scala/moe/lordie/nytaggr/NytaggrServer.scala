package moe.lordie.nytaggr

import cats.effect.ConcurrentEffect
import cats.data.Kleisli
import cats.effect._
import fs2.Stream
import org.http4s.implicits._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.Router
import org.http4s.StaticFile
import sttp.client3.http4s._
import doobie.util.transactor

object NytaggrServer {
  def stream[F[_]: ContextShift: Timer: ConcurrentEffect]
      : Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      blocker <- Stream.resource(Blocker[F])
      sttp = Http4sBackend.usingClient(client, blocker)
      xa = transactor.Transactor.fromDriverManager[F](
        "org.postgresql.Driver",
        "jdbc:postgresql:postgres",
        "postgres",
        "example"
      )

      repository = QuillRepository.impl[F](xa)
      graphQl <- Stream.resource(
        Resource.eval(CalibanGraphQL.impl[F](repository).service)
      )
      _ <- Stream.resource(
        Concurrent[F].background(ApiScraper.impl[F](repository, sttp).run)
      )

      routes = Router(
        "/api/graphql" -> graphQl,
        "/graphiql" ->
          Kleisli.liftF(
            StaticFile.fromResource("/graphiql.html", blocker, None)
          )
      ).orNotFound

      finalHttpApp = Logger.httpApp(true, true)(routes)

      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[F]
          .withHost("0.0.0.0")
          .withPort(3000)
          .withHttpApp(finalHttpApp)
          .build
          .flatMap(_ => Resource.eval(Async[F].never))
      )

    } yield exitCode
  }.drain

  def run[F[_]: ContextShift: Timer: ConcurrentEffect]: F[Unit] =
    stream[F].compile.drain

}
