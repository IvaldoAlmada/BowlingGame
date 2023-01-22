package com.game.bowling.routes

import cats.Monad
import com.game.bowling.service.GameService
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes
import org.http4s.circe.jsonEncoder
import org.http4s.dsl.Http4sDsl


object Routes {

  private val gameService: GameService = new GameService

  def gameRoutes[F[_] : Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "game" / gameId =>
        val gameById = gameService.findById(gameId.toInt)
        gameById match {
          case Some(game) => Ok (game.asJson)
          case _ => NotFound(s"No game with id $gameId found")
        }
    }
  }
}
