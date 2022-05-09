package moe.lordie.nytaggr

object CatsMain extends cats.effect.IOApp {
  import cats.effect._
  def run(args: List[String]) =
    NytaggrServer.stream[IO].compile.drain.as(ExitCode.Success)
}

// object MonixMain extends monix.eval.TaskApp {
//   import cats.effect._
//   import monix.eval._
//   def run(args: List[String]) =
//     NytaggrServer.stream[Task].compile.drain.as(ExitCode.Success)
// }

// object ZioMain extends zio.interop.catz.CatsApp {
//   import zio._
//   import zio.interop.catz._
//   import zio.interop.catz.implicits._

//   def run(args: List[String]) =
//     NytaggrServer.stream[Task].compile.drain.exitCode
// }
