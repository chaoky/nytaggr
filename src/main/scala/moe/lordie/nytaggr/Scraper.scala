package moe.lordie.nytaggr

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import cats.syntax.all._
import scala.concurrent.duration._
import cats.effect._
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3._
import sttp.client3.circe._
import io.circe.generic.auto._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

//https://docs.scala-lang.org/scala3/reference/other-new-features/trait-parameters.html :/
trait Scraper[F[_]] {
  def run: F[Unit]
}

object ApiScraper {
  def impl[F[_]: Async: Timer](
      repo: Repository[F],
      client: SttpBackend[F, Fs2Streams[F]]
  ) = new Scraper[F] {
    val apiKey = "cAHo6M2zhPBMoWBXyLT7i9BCjtTZAGM0" // TODO move this somewhere
    def buildUrl(path: String) =
      uri"https://api.nytimes.com/svc$path?api-key=$apiKey"

    case class TopStoriesResponse(results: List[TopStoriesResponseArticles])
    case class TopStoriesResponseArticles(title: String, url: String)
    def topStories: F[Response[TopStoriesResponse]] = basicRequest
      .get(
        uri"https://api.nytimes.com/svc/topstories/v2/arts.json?api-key=cAHo6M2zhPBMoWBXyLT7i9BCjtTZAGM0"
      )
      .response(asJson[TopStoriesResponse])
      .responseGetRight
      .send(client)

    def run: F[Unit] = {
      for {
        resp <- topStories
        headlines = resp.body.results.map(art =>
          HeadLine(link = art.url, title = art.title)
        )
        _ <- repo.insertNews(headlines).attempt
        _ <- Timer[F].sleep(1.hour)
      } yield ()
    }.foreverM
  }
}

object CrawlerScraper {
  def impl[F[_]: Async: Timer](repo: Repository[F]) = new Scraper[F] {
    def extractNews = {
      JsoupBrowser()
        .get("https://www.nytimes.com/")
        .extract(elementList("a"))
        .filter(_.attr("href").contains("nytimes.com/2022"))
        .filter(_.tryExtract(element("h3")) match {
          case Some(_) => true
          case None    => false
        })
        .map(e =>
          HeadLine(link = e.attr("href"), title = e.extract(allText("h3")))
        )
    }

    def run: F[Unit] = {
      for {
        extract <- Sync[F].delay(extractNews)
        _ <- repo.insertNews(extract).attempt
        _ <- Timer[F].sleep(1.hour)
      } yield ()
    }.foreverM

  }
}
