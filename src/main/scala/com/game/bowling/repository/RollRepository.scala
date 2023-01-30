package com.game.bowling.repository

import cats.data._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxApplicativeId
import com.game.bowling.model.Roll
import doobie._
import doobie.implicits._

class RollRepository(private val xa: Transactor[IO]) {

  def createRoll(number: Int, score: Int, frameId: Int): doobie.ConnectionIO[Int] =
    sql"insert into rolls (number, score, frame_id) values ($number, $score, $frameId)".update.run
  def findRollsByFrameId(frameId: Int): doobie.ConnectionIO[List[(Int, Int, Int, Int)]] =
    sql"select id, number, score, frame_id from rolls where frame_id = $frameId".query[(Int, Int, Int, Int)].to[List]
  def findRollsByFrameIdList(frameIdList: NonEmptyList[Int]): doobie.ConnectionIO[List[(Int, Int, Int, Int)]] = {
    val query = fr"""select id, number, score, frame_id from rolls where """ ++ Fragments.in(fr"frame_id", frameIdList)
    query.query[(Int, Int, Int, Int)].to[List]
  }
  private def findRollByFrameIdAndNumber(frameId: Int, number: Int) =
    sql"select id, number, score, frame_id from rolls where frame_id = $frameId and number = $number".query[(Int, Int, Int, Int)].option

  def save(roll: Roll, rollNumber: Int, frameId: Int): Option[Roll] = {
    val rollScore = roll.score.get
    val query = for {
      rollExists <- findRollByFrameIdAndNumber(frameId, rollNumber)

      _ <- rollExists match {
        case None => createRoll(rollNumber, rollScore, frameId)
        case Some(queryReturn) => queryReturn.pure[ConnectionIO]
      }

      maybeRoll <- findRollByFrameIdAndNumber(frameId, rollNumber)
    } yield {
      maybeRoll.map(roll => Roll(Some(roll._1), Some(roll._2), Some(roll._3), Some(frameId)))

    }
    query.transact(xa).unsafeRunSync()
  }
}
