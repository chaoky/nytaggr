package moe.lordie.nytaggr

import cats.effect._

class CrawlerScraperTest extends ScraperTest {
  test("crawler updates repository with news") {
    for {
      fromRepoBefore <- testRepo.news
      job <- CrawlerScraper.impl[IO](testRepo).run.start
      fromRepoAfter <- waitForUpdate
      _ <- job.cancel
      _ = assert(fromRepoAfter.length > fromRepoBefore.length)
    } yield ()
  }
}
