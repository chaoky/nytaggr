package moe.lordie.nytaggr

import caliban.Macros.gqldoc
import munit.CatsEffectSuite
import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.Method._
import org.http4s.client.dsl.io._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec._
import io.circe.syntax._
import io.circe.generic.auto._

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
