package com.game.bowling.repository

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import com.game.bowling.model.{Frame, Roll}
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

class FrameRepository(val rollRepository: RollRepository, private val xa: Transactor[IO]) {

  private def createFrame(gameId: Int, number: Option[Int], strike: Boolean) =
    sql"insert into frames (game_id, number, strike) values ($gameId, $number, $strike)".update.run
  def findFramesByGameId(gameId: Int): doobie.ConnectionIO[List[(Int, Int, Boolean, Int)]] =
    sql"select id, number, strike, game_id from frames where game_id = $gameId".query[(Int, Int, Boolean, Int)].to[List]
  private def findFrameByGameIdAndNumber(gameId: Int, number: Option[Int]) =
    sql"select id, number, strike, game_id from frames where game_id = $gameId and number = $number".query[(Int, Int, Boolean, Int)].unique
  private def findFrameById(id: Option[Int]) =
    sql"select id, number, strike, game_id from frames where id = $id".query[(Int, Int, Boolean, Int)].unique

  def findById(id: Option[Int]): IO[Frame] = {
    val query = for {
      maybeFrame <- {
        val frameIO = findFrameById(id)
        frameIO.map {
          case (id, number, strike, _) => Frame(Some(id), Some(number), strike, None)
        }
      }

      maybeRolls <- maybeFrame match {
        case frame => rollRepository.findRollsByFrameId(frame.id)
      }
    } yield {
      val rolls: List[Roll] = maybeRolls.map(roll => Roll(Some(roll._1), Some(roll._2), Some(roll._3), None))

      maybeFrame match {
        case frame => Frame(frame.id, frame.number, frame.strike, Some(rolls))
      }
    }
    query.transact(xa)
  }

  def save(frame: Frame, gameId: Int): IO[Frame] = {
    val query = for {
      frameExists <- findFrameByGameIdAndNumber(gameId, frame.number)

      _ <- frameExists match {
        case _ => createFrame(gameId, frame.number, frame.strike)
      }
      maybeFrame <- {
        val frameIO = findFrameByGameIdAndNumber(gameId, frame.number)
        frameIO.map {
          case (id, number, strike, _) => Frame(Some(id), Some(number), strike, None)
        }
      }
    } yield {
      maybeFrame
    }
    query.transact(xa)
  }
}
