package moe.lordie.nytaggr

import sttp.client3.http4s._
import cats.effect._
import org.http4s.ember.client.EmberClientBuilder
import sttp.client3.logging.slf4j.Slf4jLoggingBackend

class ApiScraperTest extends ScraperTest {
  val setup = for {
    client <- EmberClientBuilder.default[IO].build
    blocker <- Blocker[IO]
    sttp = Slf4jLoggingBackend(Http4sBackend.usingClient(client, blocker))
  } yield (sttp)

  test("api updates repository with news") {
    setup.use { sttp =>
      for {
        fromRepoBefore <- testRepo.news
        job <- ApiScraper.impl[IO](testRepo, sttp, sys.env("NYT_SECRET")).run.start
        fromRepoAfter <- waitForUpdate
        _ <- job.cancel
        _ = assert(fromRepoAfter.length > fromRepoBefore.length)
      } yield ()
    }
  }
}
