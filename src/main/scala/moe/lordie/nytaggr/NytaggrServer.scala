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
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import sttp.client3.http4s._
import org.flywaydb.core.Flyway

case object Config {
  val dbUrl = sys.env("POSTGRES_URL")
  val dbUser = sys.env("POSTGRES_USER")
  val dbPassword = sys.env("POSTGRES_PASSWORD")
  val nytSecrept = sys.env("NYT_SECRET")
}

object NytaggrServer {
  def stream[F[_]: ContextShift: Timer: ConcurrentEffect]
      : Stream[F, Nothing] = {
    for {
      blocker <- Stream.resource(Blocker[F])
      ec <- Stream.resource(ExecutionContexts.cachedThreadPool[F])
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      transactor <- Stream.resource(
        HikariTransactor.newHikariTransactor[F](
          "org.postgresql.Driver",
          Config.dbUrl,
          Config.dbUser,
          Config.dbPassword,
          ec,
          blocker
        )
      )
      _ <- Stream.eval(
        transactor.configure(ds =>
          Sync[F].delay {
            val flyWay = Flyway.configure().dataSource(ds).load()
            flyWay.migrate()
            ()
          }
        )
      )

      sttp = Http4sBackend.usingClient(client, blocker)
      repository = QuillRepository.impl[F](transactor)
      graphQl <- Stream.resource(
        Resource.eval(CalibanGraphQL.impl[F](repository).service)
      )
      _ <- Stream.resource(
        Concurrent[F].background(CrawlerScraper.impl[F](repository).run)
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
}
