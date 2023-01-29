package com.game.bowling.routes

import cats.effect.{Concurrent, IO}
import cats.implicits._
import com.game.bowling.model.{Game, Roll}
import com.game.bowling.repository.{FrameRepository, GameRepository, RollRepository}
import com.game.bowling.service.{FrameService, GameService, RollService}
import doobie.Transactor
import io.circe.generic.auto.{exportDecoder, exportEncoder}
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

object Routes {

  private val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:postgres",
    "docker",
    "docker"
  )

  private val rollRepository = new RollRepository(xa)
  private val frameRepository = new FrameRepository(rollRepository, xa)
  private val gameRepository = new GameRepository(frameRepository, rollRepository, xa)

  private val rollService = new RollService(rollRepository)
  private val frameService = new FrameService(frameRepository, rollService)
  private val gameService = new GameService(gameRepository, frameService)

  def gameRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val gameDecoder: EntityDecoder[F, Game] = jsonOf[F, Game]
    implicit val rollDecoder: EntityDecoder[F, Roll] = jsonOf[F, Roll]
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
          roll <- req.as[Roll]
          gameWithRoll = gameService.roll(roll, gameId.toInt)
          res <- gameWithRoll match {
            case Some(game) => Ok(game)
            case None => NoContent()
          }
        } yield res
      case DELETE -> Root / "game" / gameId =>
        val deletedFiles = gameService.delete(gameId.toInt)
        Ok(s"Deleted $deletedFiles games")
    }
  }
}
