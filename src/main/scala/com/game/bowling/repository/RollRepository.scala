package com.game.bowling.repository

import cats.data._
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import com.game.bowling.model.Roll
import doobie._
import doobie.implicits._

class RollRepository(private val xa: Transactor[IO]) {

  def createRoll(number: Int, score: Option[Int], frameId: Option[Int]): doobie.ConnectionIO[Int] =
    sql"insert into rolls (number, score, frame_id) values ($number, $score, $frameId)".update.run
  def findRollsByFrameId(frameId: Option[Int]): doobie.ConnectionIO[List[(Int, Int, Int, Int)]] =
    sql"select id, number, score, frame_id from rolls where frame_id = $frameId".query[(Int, Int, Int, Int)].to[List]
  def findRollsByFrameIdList(frameIdList: NonEmptyList[Int]): doobie.ConnectionIO[List[(Int, Int, Int, Int)]] = {
    val query = fr"""select id, number, score, frame_id from rolls where """ ++ Fragments.in(fr"frame_id", frameIdList)
    query.query[(Int, Int, Int, Int)].to[List]
  }
  private def findRollByFrameIdAndNumber(frameId: Option[Int], number: Int) =
    sql"select id, number, score, frame_id from rolls where frame_id = $frameId and number = $number".query[(Int, Int, Int, Int)].unique

  def save(roll: Roll, rollNumber: Int, frameId: Option[Int]): IO[Roll] = {
    val rollScore = roll.score
    val query = for {
      rollExists <- findRollByFrameIdAndNumber(frameId, rollNumber)

      _ <- rollExists match {
        case _ => createRoll(rollNumber, rollScore, frameId)
      }

      maybeRoll <- {
        val rollIO = findRollByFrameIdAndNumber(frameId, rollNumber)
        rollIO.map {
          case (id, number, score, _) => Roll(Some(id), Some(number), Some(score), frameId)
        }
      }
    } yield {
      maybeRoll
    }
    query.transact(xa)
  }
}
