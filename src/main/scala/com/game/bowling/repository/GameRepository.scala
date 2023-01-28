package com.game.bowling.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxApplicativeId
import com.game.bowling.model.{Frame, Game, Row}
import doobie.{ConnectionIO, Transactor}
import doobie.implicits._

import scala.runtime.Nothing$

class GameRepository {

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:postgres",
    "docker",
    "docker"
  )


  //Game
  def findGameById(id: Int) =
    sql"select id, name from games where id = $id".query[(Int, String)].option
  def findGameByName(name: String) =
    sql"select id, name from games where name = $name".query[(Int, String)].option

  def createGame(name: String) =
    sql"insert into games (name) values ($name)".update.run


  //Frame
  def createFrame(gameId: Int) =
    sql"insert into frames (game_id) values ($gameId)".update.run
  def findFramesByGameId(gameId: Int) =
    sql"select id, game_id from frames where game_id = $gameId".query[(Int, Int)].to[List]

  //Row
  def createRow(score: Int, frameId: Int) =
    sql"insert into rows_ (score, frame_id) values ($score, $frameId)".update.run
  def findRowsByFrameId(frameId: Int) =
    sql"select id, score, frame_id from rows_ where frame_id = $frameId".query[(Int, Int, Int)].to[List]


  def findById(id: Int): Option[Game] = {

    val query = for {
      maybeGame <- findGameById(id)

      maybeFrames <- maybeGame match {
        case Some((gameId, _)) => findFramesByGameId(gameId)
        case None => List.empty[(Int, Int)].pure[ConnectionIO]
      }
      maybeRows <- maybeFrames match {
        case Nil => List.empty[(Int, Int, Int)].pure[ConnectionIO]
        case list => findRowsByFrameId(list.head._1)
      }
    } yield {
      val rows = maybeRows.map {
        case row => Row(Some(row._1), Some(row._2))
        case _ => Row(None, None)
      }

      val frames: List[Frame] = maybeFrames.map {
        case frame => Frame(Some(frame._1), Some(rows))
        case _ => Frame(None, None)
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

      saveGame <- gameExists match {
        case None =>
          createGame(game.name.get)
      }

      maybeGame <- findGameByName(game.name.get)


    } yield {
      maybeGame.map {
        case (id, name) => Game(Some(id), Some(name), None)
      }
    }

    query.transact(xa).unsafeRunSync()
  }

  def delete(id: Int): IO[Int] = {
    val deleteGame: doobie.ConnectionIO[Int] =
      sql"delete from games where id = $id".update.run
    deleteGame.transact(xa)
  }

  def update(game: Game): IO[Int] = {
    val updateGame: doobie.ConnectionIO[Int] =
      sql"update games set (name) = ${game.id}".update.run
    updateGame.transact(xa)
  }

}
