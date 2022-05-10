package moe.lordie.nytaggr

import scala.concurrent.duration._
import munit.CatsEffectSuite
import cats.effect._

class ScraperTest extends CatsEffectSuite {
  override val munitTimeout = 3.minutes
  val testRepo = TestRepository.impl[IO]
  def waitForUpdate: IO[List[HeadLine]] =
    IO.sleep(500.milliseconds)
      .flatMap(_ => testRepo.news)
      .map(_.length > 0)
      .flatMap(isUpdated => if (isUpdated) testRepo.news else waitForUpdate)
}
