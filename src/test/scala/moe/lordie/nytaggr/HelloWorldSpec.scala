package moe.lordie.nytaggr

import sttp.client3.http4s._
import scala.concurrent.duration._
import caliban.Macros.gqldoc
import munit.CatsEffectSuite
import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.Method._
import org.http4s.client.dsl.io._
import org.http4s.circe._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.circe.CirceEntityCodec._
import io.circe.syntax._
import io.circe.generic.auto._
import doobie.util.transactor.Transactor
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres

//TODO tagless final tests?
object EmbeddedPostgresResource {
  def apply() = Resource.make(setup)(pg => IO(pg.close()))
  private def setup = {
    val pg = EmbeddedPostgres.start()
    val conn = pg.getPostgresDatabase().getConnection()
    val s = conn.createStatement()
    s.executeUpdate("""CREATE TABLE headline (link VARCHAR PRIMARY KEY, title VARCHAR NOT NULL);""")
    IO(conn)
  }
}

class PersistantRepositoryTest extends CatsEffectSuite {
  val setup = for {
    blocker <- Blocker[IO]
    pg <- EmbeddedPostgresResource()
    xa = Transactor.fromConnection[IO](pg, blocker)
    repo = QuillRepository.impl[IO](xa)
  } yield (repo)

  test("insert news") {
    val news = List(
      HeadLine("Lilian", "https://youtu.be/z97qLNXeAMQ"),
      HeadLine("OwO", "https://youtu.be/iYcRQBrUbd0")
    )
    setup.use { repo =>
      for {
        _ <- repo.insertNews(news)
        fromRepo <- repo.news
        _ = assert(
          fromRepo.containsSlice(news)
        )
      } yield ()
    }
  }
}

class ApiScraperTest extends CatsEffectSuite {
  val setup = for {
    client <- EmberClientBuilder.default[IO].build
    blocker <- Blocker[IO]
    sttp = Http4sBackend.usingClient(client, blocker)
    testRepo = TestRepository.impl[IO]
  } yield (sttp, testRepo)

  // TODO check every half a second up to a timeout instead of a blank 5 seconds
  test("updates repository with news") {
    setup.use { case (sttp, testRepo) =>
      for {
        fromRepoBefore <- testRepo.news
        job <- ApiScraper.impl[IO](testRepo, sttp).run.start
        _ <- IO.sleep(5.seconds)
        _ <- job.cancel
        fromRepoAfter <- testRepo.news
        _ = assert(fromRepoAfter.length > fromRepoBefore.length)
      } yield ()
    }
  }
}

class CalibanGraphQLTest extends CatsEffectSuite {
  val query = gqldoc("""{news {title, link }}""")
  case class Payload(query: String)
  case class Response(data: ReponseNews)
  case class ReponseNews(news: List[HeadLine])
  implicit val userDecoder = jsonOf[IO, Response]

  test("news query") {
    for {
      route <- CalibanGraphQL.impl[IO](TestRepository.impl[IO]).service
      req <- POST(Payload(query).asJson, uri"/")
      resp <- route.orNotFound(req)
      _ = assert(resp.status == Status.Ok)
      _ <- resp.as[Response]
    } yield ()
  }
}
