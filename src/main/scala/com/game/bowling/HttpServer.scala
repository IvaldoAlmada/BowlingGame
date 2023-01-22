package com.game.bowling

import cats.effect.{ExitCode, IO, IOApp}
import com.game.bowling.routes.Routes
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

object HttpServer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val apis = Router(
      "/api" -> Routes.gameRoutes[IO]
    ).orNotFound

    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8081, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}
