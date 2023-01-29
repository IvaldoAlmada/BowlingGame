package com.game.bowling.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxApplicativeId
import com.game.bowling.model.{Frame, Game, Roll}
import doobie.{ConnectionIO, Transactor}
import doobie.implicits._

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._

class GameRepository {

  private val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:postgres",
    "docker",
    "docker"
  )


  //Game
  private def findGameById(id: Int) =
    sql"select id, name from games where id = $id".query[(Int, String)].option
  private def findGameByName(name: String) =
    sql"select id, name from games where name = $name".query[(Int, String)].option

  private def createGame(name: String) =
    sql"insert into games (name) values ($name)".update.run
  private def deleteGame(id: Int) =
    sql"delete from games where id = $id".update.run


  //Frame
  private def createFrame(gameId: Int, number: Int) =
    sql"insert into frames (game_id, number) values ($gameId, $number)".update.run
  private def findFramesByGameId(gameId: Int) =
    sql"select id, number, game_id from frames where game_id = $gameId".query[(Int, Int, Int)].to[List]
  private def findFrameByGameIdAndNumber(gameId: Int, number: Int) =
    sql"select id, number, game_id from frames where game_id = $gameId and number = $number".query[(Int, Int, Int)].option

  //Roll
  private def createRoll(number: Int, score: Int, frameId: Int) =
    sql"insert into rolls (number, score, frame_id) values ($number, $score, $frameId)".update.run
  private def findRollsByFrameId(frameId: Int) =
    sql"select id, number, score, frame_id from rolls where frame_id = $frameId".query[(Int, Int, Int, Int)].to[List]

  private def findRollsByFrameIdList(frameIdList: NonEmptyList[Int]) = {
    val query = fr"""select id, number, score, frame_id
        from rolls
        where """ ++ Fragments.in(fr"frame_id", frameIdList)

    query.query[(Int, Int, Int, Int)].to[List]
  }

  private def findRollByFrameIdAndNumber(frameId: Int, number: Int) =
    sql"select id, number, score, frame_id from rolls where frame_id = $frameId and number = $number".query[(Int, Int, Int, Int)].option


  def findById(id: Int): Option[Game] = {

    val query = for {
      maybeGame <- findGameById(id)

      maybeFrames <- maybeGame match {
        case Some((gameId, _)) => findFramesByGameId(gameId)
        case None => List.empty[(Int, Int, Int)].pure[ConnectionIO]
      }
      maybeRolls <- maybeFrames match {
        case Nil => List.empty[(Int, Int, Int, Int)].pure[ConnectionIO]
        case frameList =>
          val frameIds = frameList.map(tuple => tuple._1)
          findRollsByFrameIdList(NonEmptyList.fromList(frameIds).get)
      }
    } yield {
      val rolls = maybeRolls.map {
        case roll => Roll(Some(roll._1), Some(roll._2), Some(roll._3), Some(roll._4))
        case _ => Roll(None, None, None, None)
      }

      val frames: List[Frame] = maybeFrames.map {
        case frame =>
          val frameRolls = rolls.filter(roll => roll.frameId.get == frame._1)
          Frame(Some(frame._1), Some(frame._2), Some(frameRolls))
        case _ => Frame(None, None, None)
      }

      maybeGame.map {
        case (id, name) => Game(Some(id), Some(name), Some(frames))
      }

    }
    query.transact(xa).unsafeRunSync()
  }

  def save(game: Game): Option[Game] = {
    val query = for {
      gameExists <- findGameByName(game.name.get)

      _ <- gameExists match {
        case None => createGame(game.name.get)
      }

      maybeGame <- findGameByName(game.name.get)


    } yield {
      maybeGame.map {
        case (id, name) => Game(Some(id), Some(name), None)
      }
    }

    query.transact(xa).unsafeRunSync()
  }

  def save(frame: Frame, gameId: Int): Option[Frame] = {
    val query = for {
      frameExists <- findFrameByGameIdAndNumber(gameId, frame.number.get)

      _ <- frameExists match {
        case None => createFrame(gameId, frame.number.get)
      }

      maybeFrame <- findFrameByGameIdAndNumber(gameId, frame.number.get)
    } yield {
      maybeFrame.map {
        case (id, number, _) => Frame(Some(id), Some(number), None)
      }
    }

    query.transact(xa).unsafeRunSync()
  }

  def save(roll: Roll, rollNumber: Int, frameId: Int): Option[Roll] = {
    val rollScore = roll.score.get
    val query = for {
      rollExists <- findRollByFrameIdAndNumber(frameId, rollNumber)

      _ <- rollExists match {
        case None => createRoll(rollNumber, rollScore, frameId)
      }

      maybeRoll <- findRollByFrameIdAndNumber(frameId, rollNumber)
    } yield {
      maybeRoll.map {
        case (id, number, score, _) => Roll(Some(id), Some(number), Some(score), None)
      }
    }
    query.transact(xa).unsafeRunSync()
  }

  def delete(id: Int): Int = {
    val query = for {
      deleteResult <- deleteGame(id)
    } yield {
      deleteResult
    }
    query.transact(xa).unsafeRunSync()
  }

}
