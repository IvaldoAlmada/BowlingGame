package com.game.bowling.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object Routes {

  def scoreRoutes[F[_] : Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "score" / UUIDVar(gameId) => ???

    }
  }
}
