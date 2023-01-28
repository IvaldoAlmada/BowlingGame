package com.game.bowling.routes

import cats.effect.Concurrent
import cats.effect.unsafe.implicits.global
import cats.implicits._
import com.game.bowling.model.{Frame, Game}
import com.game.bowling.service.GameService
import doobie.Read
import io.circe.generic.auto.{exportDecoder, exportEncoder}
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}


object Routes {

  private val gameService: GameService = new GameService

  def gameRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val gameDecoder: EntityDecoder[F, Game] = jsonOf[F, Game]
    implicit val frameDecoder: EntityDecoder[F, Frame] = jsonOf[F, Frame]
    HttpRoutes.of[F] {
      case GET -> Root / "game" / gameId =>
        val gameById = gameService.findById(gameId.toInt)
        gameById match {
          case Some(game) => Ok(game.asJson)
          case _ => NotFound(s"No game with id $gameId found")
        }
      case GET -> Root / "game" / gameId / "score" =>
        val gameScore = gameService.calculateScore(gameId.toInt)
        gameScore match {
          case Some(score) => Ok(score.asJson)
          case _ => NotFound(s"Is Not possible to calculate score from game: $gameId")
        }
      case req@POST -> Root / "game" =>
        for {
          game <- req.as[Game]
          savedGame = gameService.save(game)
          res <- Ok(savedGame)
        } yield res
      case req@PUT -> Root / "game" / gameId / "roll" =>
        for {
          frame <- req.as[Frame]
          updatedGame = gameService.addRoll(frame, gameId.toInt).unsafeRunSync()
          res <- Ok(updatedGame)
        } yield res
      case DELETE -> Root / "game" / gameId =>
        val deletedGame = gameService.delete(gameId.toInt).unsafeRunSync()
        Ok(deletedGame)
    }
  }
}
