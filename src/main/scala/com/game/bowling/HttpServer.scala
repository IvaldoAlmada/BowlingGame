package com.game.bowling

import cats.effect.{ExitCode, IO, IOApp}
import com.game.bowling.routes.Routes
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.LoggerFactory

object HttpServer extends IOApp {

  private val logger = LoggerFactory.getLogger(getClass.getName)

  override def run(args: List[String]): IO[ExitCode] = {
    logger.info("starting httpServer")
    val apis = Router(
      "/api" -> Routes.gameRoutes[IO]
    ).orNotFound

    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}