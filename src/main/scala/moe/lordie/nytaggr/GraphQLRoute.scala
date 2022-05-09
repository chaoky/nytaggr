package moe.lordie.nytaggr

import org.http4s.HttpRoutes
import caliban._
import caliban.interop.cats.implicits._
import cats.effect._
import cats.syntax.all._
import zio.{Runtime, ZEnv}
import caliban.GraphQL
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.wrappers.Wrappers._

trait GraphQLRoute[F[_]] {
  def service: F[HttpRoutes[F]]
}

object CalibanGraphQL {
  type ZioEnv = zio.console.Console
  def impl[F[_]: ConcurrentEffect](repository: Repository[F]) =
    new GraphQLRoute[F] {
      implicit val runtime: Runtime[ZEnv] = Runtime.default
      case class Queries(news: F[List[HeadLine]])
      def queries = Queries(repository.news)

      def api: GraphQL[Any] = graphQL(RootResolver(queries))

      def service: F[HttpRoutes[F]] =
        api
          .withWrapper(printErrors)
          .interpreterAsync
          .map(_.provideLayer(zio.console.Console.live))
          .map(Http4sAdapter.makeHttpServiceF(_))
    }
}
//
//TODO Sangria implementation
// object SangriaGraphQL {
//   import sangria.macros.derive._
//   import sangria.schema._

//   val NewsType =
//     deriveObjectType[Unit, HeadLine](
//       ObjectTypeDescription("News Article"),
//       DocumentField("title", "head line"),
//       DocumentField("link", "nytimes url")
//     )

//   val QueryType =
//     ObjectType(
//       "Query",
//       fields[Unit, HeadLine](
//         Field(
//           "news",
//           ListType(NewsType),
//           description = Some("all news"),
//           resolve = _.ctx.all
//         )
//       )
//     )

//   val schema = Schema(QueryType)

//   def impl[F[_]: ConcurrentEffect] = new GraphQL[F] {
//     def api: F[GraphQLInterpreter[Any, CalibanError]] = graphQL(
//       RootResolver(queries)
//     ).interpreterAsync[F]

//     def service: F[HttpRoutes[F]] =
//       api.map(Http4sAdapter.makeHttpServiceF[F, Throwable](_))
//   }
// }
