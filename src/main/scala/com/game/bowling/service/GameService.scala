package com.game.bowling.service

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
        val lastFrameFromDB: Option[Frame] = frames.reduceOption((a1, a2) => if (a1.number > a2.number) a1 else a2)
        val lastFrame = lastFrameFromDB match {
          case Some(frame) => frame
          case _ => createFrame(Frame(None, Some(1), None), id).get
        }

        val rolls: List[Roll] = lastFrame.rolls match {
          case Some(rollList) => rollList
          case _ => List.empty[Roll]
        }

        val lastRollFromDB: Option[Roll] = rolls.reduceOption((a1, a2) => if(a1.number > a2.number) a1 else a2)
        val lastRoll = lastRollFromDB match {
          case Some(roll) => roll
          case _ => createRoll(roll, lastFrame.id.get)
        }

        findById(id)
      }
      case _ =>
        None
    }
  }

  def createFrame(frame: Frame, gameId: Int): Option[Frame] = {
    gameRepository.save(frame, gameId)
  }

  def createRoll(roll: Roll, frameId: Int): Option[Roll] = {
    gameRepository.save(roll, frameId)
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

  def delete(id: Int): Int = {
    gameRepository.delete(id)
  }
}
