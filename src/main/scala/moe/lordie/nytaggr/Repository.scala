package moe.lordie.nytaggr

import cats.syntax.all._
import cats.effect._
import doobie.util.transactor.Transactor
import doobie.implicits._

//https://www.youtube.com/watch?v=1WVjkP_G2cA

case class HeadLine(link: String, title: String)

trait Repository[F[_]] {
  def news: F[List[HeadLine]]
  def insertNews(news: List[HeadLine]): F[Unit]
}

object QuillRepository {

  import io.getquill._
  val ctx = new doobie.quill.DoobieContext.Postgres(LowerCase)
  import ctx._

  def impl[F[_]: Sync](xa: Transactor[F]) = new Repository[F] {
    def news: F[List[HeadLine]] = {
      def q = ctx.run(quote(query[HeadLine]))
      q.transact(xa)
    }

    def insertNews(news: List[HeadLine]): F[Unit] = {
      def q = ctx.run(liftQuery(news).foreach(e => query[HeadLine].insert(e)))
      q.transact(xa).map(_ => (()))
    }
  }
}

object TestRepository {
  var newsList: List[HeadLine] = List()

  def impl[F[_]: LiftIO] = new Repository[F] {
    def news: F[List[HeadLine]] =
      IO(newsList).to[F]

    def insertNews(news: List[HeadLine]): F[Unit] =
      IO { newsList = news }.map(_ => (())).to[F]
  }
}
