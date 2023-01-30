package com.game.bowling

import cats.effect.{ExitCode, IO, IOApp}
import com.game.bowling.routes.Routes
import com.typesafe.scalalogging.Logger
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

object HttpServer extends IOApp {

  private val logger = Logger(getClass.getName)

  override def run(args: List[String]): IO[ExitCode] = {
    val apis = Router(
      "/api" -> Routes.gameRoutes[IO]
    ).orNotFound

    logger.info("starting httpServer")

    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}