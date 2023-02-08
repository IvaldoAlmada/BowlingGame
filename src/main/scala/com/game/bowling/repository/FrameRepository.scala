package com.game.bowling.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxApplicativeId
import com.game.bowling.model.{Frame, Roll}
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

class FrameRepository(val rollRepository: RollRepository, private val xa: Transactor[IO]) {

  private def createFrame(gameId: Int, number: Int, strike: Boolean) =
    sql"insert into frames (game_id, number, strike) values ($gameId, $number, $strike)".update.run
  def findFramesByGameId(gameId: Int): doobie.ConnectionIO[List[(Int, Int, Boolean, Int)]] =
    sql"select id, number, strike, game_id from frames where game_id = $gameId".query[(Int, Int, Boolean, Int)].to[List]
  private def findFrameByGameIdAndNumber(gameId: Int, number: Int) =
    sql"select id, number, strike, game_id from frames where game_id = $gameId and number = $number".query[(Int, Int, Boolean, Int)].option
  private def findFrameById(id: Int) =
    sql"select id, number, strike, game_id from frames where id = $id".query[(Int, Int, Boolean, Int)].option

  def findById(id: Int): IO[Option[Frame]] = {
    val query = for {
      maybeFrame <- findFrameById(id)

      maybeRolls <- maybeFrame match {
        case Some((frameId, _, _, _)) => rollRepository.findRollsByFrameId(frameId)
        case None => List.empty[(Int, Int, Int, Int)].pure[ConnectionIO]
      }
    } yield {
      val rolls: List[Roll] = maybeRolls.map(roll => Roll(Some(roll._1), Some(roll._2), Some(roll._3), None))

      maybeFrame match {
        case Some(frame) => Some(Frame(Some(frame._1), Some(frame._2), frame._3, Some(rolls)))
        case _ => None
      }
    }
    query.transact(xa)
  }

  def save(frame: Frame, gameId: Int): Option[Frame] = {
    val query = for {
      frameExists <- findFrameByGameIdAndNumber(gameId, frame.number.get)

      _ <- frameExists match {
        case None => createFrame(gameId, frame.number.get, frame.strike)
        case Some(queryReturn) => queryReturn.pure[ConnectionIO]
      }
      maybeFrame <- findFrameByGameIdAndNumber(gameId, frame.number.get)
    } yield {
      maybeFrame.map {
        case (id, number, strike, _) => Frame(Some(id), Some(number), strike, None)
      }
    }
    query.transact(xa).unsafeRunSync()
  }
}
