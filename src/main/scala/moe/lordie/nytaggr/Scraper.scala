package moe.lordie.nytaggr

//https://docs.scala-lang.org/scala3/reference/other-new-features/trait-parameters.html :/
trait Scraper[F[_]] {
  def run: F[Unit]
}

object ApiScraper {
  import cats.syntax.all._
  import scala.concurrent.duration._
  import cats.effect._
  import sttp.capabilities.fs2.Fs2Streams
  import sttp.client3._
  import sttp.client3.circe._
  import io.circe.generic.auto._

  def impl[F[_]: Async: Timer](
      repo: Repository[F],
      client: SttpBackend[F, Fs2Streams[F]]
  ) = new Scraper[F] {
    val apiKey = "cAHo6M2zhPBMoWBXyLT7i9BCjtTZAGM0" // TODO move this somewhere
    def buildUrl(path: String) =
      uri"https://api.nytimes.com/svc$path?api-key=$apiKey"

    def run: F[Unit] = {
      for {
        resp <- topStories
        headlines = resp.body.results.map(art =>
          HeadLine(link = art.url, title = art.title)
        )
        _ <- repo.insertNews(headlines)
        _ <- Timer[F].sleep(1.minute)
      } yield ()
    }.foreverM

    case class TopStoriesResponse(results: List[TopStoriesResponseArticles])
    case class TopStoriesResponseArticles(title: String, url: String)
    def topStories: F[Response[TopStoriesResponse]] = basicRequest
      .get(uri"https://api.nytimes.com/svc/topstories/v2/arts.json?api-key=cAHo6M2zhPBMoWBXyLT7i9BCjtTZAGM0")
      .response(asJson[TopStoriesResponse])
      .responseGetRight
      .send(client)
  }
}
