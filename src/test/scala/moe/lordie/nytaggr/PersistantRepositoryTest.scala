package moe.lordie.nytaggr

import munit.CatsEffectSuite
import cats.effect._
import doobie.util.transactor.Transactor
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres

object EmbeddedPostgresResource {
  def apply() = Resource.make(IO(setup))(pg => IO(pg.close()))
  private def setup = {
    val pg = EmbeddedPostgres.start()
    val conn = pg.getPostgresDatabase().getConnection()
    conn
      .createStatement()
      .executeUpdate(
        """CREATE TABLE headline (link VARCHAR PRIMARY KEY, title VARCHAR NOT NULL);"""
      )
    pg
  }
}

class PersistantRepositoryTest extends CatsEffectSuite {
  val setup = for {
    blocker <- Blocker[IO]
    pg <- EmbeddedPostgresResource()
    xa = Transactor.fromConnection[IO](pg.getPostgresDatabase().getConnection(), blocker)
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
