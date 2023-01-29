package com.game.bowling.repository

import cats.data._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxApplicativeId
import com.game.bowling.model.{Frame, Game, Roll}
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

class GameRepository(private val frameRepository: FrameRepository, private val rollRepository: RollRepository, private val xa: Transactor[IO]) {

  private def findGameById(id: Int) =
    sql"select id, name, complete from games where id = $id".query[(Int, String, Boolean)].option
  private def findGameByName(name: String) =
    sql"select id, name, complete from games where name = $name".query[(Int, String, Boolean)].option
  private def createGame(name: String) =
    sql"insert into games (name, complete) values ($name, false)".update.run
  private def deleteGame(id: Int) =
    sql"delete from games where id = $id".update.run

  private def completeGame(id: Int) =
    sql"update games set complete = true where id = $id".update.run

  def findById(id: Int): Option[Game] = {
    val query = for {
      maybeGame <- findGameById(id)

      maybeFrames <- maybeGame match {
        case Some((gameId, _, _)) => frameRepository.findFramesByGameId(gameId)
        case None => List.empty[(Int, Int, Boolean, Int)].pure[ConnectionIO]
      }
      maybeRolls <- maybeFrames match {
        case Nil => List.empty[(Int, Int, Int, Int)].pure[ConnectionIO]
        case frameList =>
          val frameIds = frameList.map(tuple => tuple._1)
          rollRepository.findRollsByFrameIdList(NonEmptyList.fromList(frameIds).get)
      }
    } yield {
      val rolls = maybeRolls.map {
        case roll => Roll(Some(roll._1), Some(roll._2), Some(roll._3), Some(roll._4))
        case _ => Roll(None, None, None, None)
      }

      val frames: List[Frame] = maybeFrames.map {
        case frame =>
          val frameRolls = rolls.filter(roll => roll.frameId.get == frame._1)
          Frame(Some(frame._1), Some(frame._2), frame._3, Some(frameRolls))
        case _ => Frame(None, None, strike = false, None)
      }

      maybeGame.map {
        case (id, name, complete) => Game(Some(id), Some(name), complete = complete, Some(frames))
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
        case (id, name, complete) => Game(Some(id), Some(name), complete, None)
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

  def complete(gameId: Int): Int = {
    val query = for {
      completeResult <- completeGame(gameId)
    } yield {
      completeResult
    }
    query.transact(xa).unsafeRunSync()
  }
}