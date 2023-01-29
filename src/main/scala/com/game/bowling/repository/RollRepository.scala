package com.game.bowling.repository

import cats.data._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.game.bowling.model.Roll
import doobie._
import doobie.implicits._

class RollRepository {

  private val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:postgres",
    "docker",
    "docker"
  )

  //Roll
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
      }

      maybeRoll <- findRollByFrameIdAndNumber(frameId, rollNumber)
    } yield {
      maybeRoll.map {
        case (id, number, score, frameId) => Roll(Some(id), Some(number), Some(score), Some(frameId))
      }
    }
    query.transact(xa).unsafeRunSync()
  }

}
