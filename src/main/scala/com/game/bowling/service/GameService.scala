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

  def roll(rollToSave: Roll, gameId: Int): Option[Game] = {
    val game = findById(gameId)
    game match {
      case Some(game) =>
        val frames: List[Frame] = game.frames match {
          case Some(frameList) => frameList
          case _ => List.empty[Frame]
        }
        val lastFrameFromDB: Option[Frame] = frames.reduceOption((a1, a2) => if (a1.number > a2.number) a1 else a2)

        val lastFrame: Option[Frame] = lastFrameFromDB match {
          case Some(frame) =>
            val nextFrameNumber = Some(frame.number.get + 1)
            val nextFrame = Frame(None, nextFrameNumber, None)
            createFrame(nextFrame, gameId)
          case _ => createFrame(Frame(None, Some(1), None), gameId)
        }

        val rolls: List[Roll] = lastFrame.get.rolls match {
          case Some(rollList) => rollList
          case _ => List.empty[Roll]
        }

        val lastRollFromDB: Option[Roll] = rolls.reduceOption((a1, a2) => if (a1.number > a2.number) a1 else a2)

        lastRollFromDB match {
          case Some(roll) =>
            val nextRollNumber = roll.number.get + 1
            val nextRoll = Roll(None, None, rollToSave.score, None)
            createRoll(nextRoll, nextRollNumber, lastFrame.get.id.get)
          case _ => createRoll(rollToSave, 1, lastFrame.get.id.get)
        }

        findById(gameId)
      case _ =>
        None
    }
  }

  def createFrame(frame: Frame, gameId: Int): Option[Frame] = {
    gameRepository.save(frame, gameId)
  }

  def createRoll(roll: Roll, rollNumber: Int, frameId: Int): Option[Roll] = {
    gameRepository.save(roll, rollNumber, frameId)
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
