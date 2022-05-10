package moe.lordie.nytaggr

import scala.concurrent.duration._
import munit.CatsEffectSuite
import cats.effect._

class CrawlerScraperTest extends CatsEffectSuite {
  override val munitTimeout = 1.minute
  val testRepo = TestRepository.impl[IO]

  // TODO check every half a second up to a timeout instead of a blank 5 seconds
  test("crawler updates repository with news") {
    for {
      fromRepoBefore <- testRepo.news
      job <- CrawlerScraper.impl[IO](testRepo).run.start
      _ <- IO.sleep(5.seconds)
      _ <- job.cancel
      fromRepoAfter <- testRepo.news
      _ = assert(fromRepoAfter.length > fromRepoBefore.length)
    } yield ()
  }
}
