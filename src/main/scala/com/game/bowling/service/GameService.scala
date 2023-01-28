package com.game.bowling.service

import cats.effect.IO
import com.game.bowling.model.{Frame, Game, Roll}
import com.game.bowling.repository.GameRepository

import scala.math.Ordered.orderingToOrdered

class GameService() {

  private val gameRepository: GameRepository = new GameRepository

  def findById(id: Int): Option[Game] = {
    gameRepository.findById(id)
  }

  def save(game: Game): Option[Game] = {
    gameRepository.save(game)
  }

  def roll(roll: Roll, id: Int): Option[Game] = {
    val game = findById(id)
    game match {
      case Some(game) => {
        val frames: List[Frame] = game.frames match {
          case Some(frameList) => frameList
          case _ => List.empty[Frame]
        }
        val lastFrameOpt: Option[Frame] = frames.reduceOption((a1, a2) => if (a1.number > a2.number) a1 else a2)
        val lastFrame = lastFrameOpt match {
          case _ => createFrame(Frame(None, Some(1), None), id)
        }

        lastFrame


      }
      case _ =>
        None
    }
  }

  def createFrame(frame: Frame, gameId: Int): Option[Frame] = {
    gameRepository.save(frame, gameId)
  }

  def calculateScore(id: Int): Option[Int] = {
    val game = findById(id)
    game match {
      case Some(game) =>
        val score = game.frames.get.flatMap(frame => frame.rolls.get).map(roll => roll.score.get).sum
        Some(score)
      case _ =>
        None
    }
  }

  def delete(id: Int): IO[Int] = {
    gameRepository.delete(id)
  }
}
