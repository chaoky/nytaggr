package moe.lordie.nytaggr

import sttp.client3.http4s._
import scala.concurrent.duration._
import munit.CatsEffectSuite
import cats.effect._
import org.http4s.ember.client.EmberClientBuilder

class ApiScraperTest extends CatsEffectSuite {
  override val munitTimeout = 1.minute
  val setup = for {
    client <- EmberClientBuilder.default[IO].build
    blocker <- Blocker[IO]
    sttp = Http4sBackend.usingClient(client, blocker)
    testRepo = TestRepository.impl[IO]
  } yield (sttp, testRepo)

  // TODO check every half a second up to a timeout instead of a blank 5 seconds
  test("api updates repository with news") {
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
