package com.game.bowling.service

import com.game.bowling.model.{Frame, Game, Roll}
import com.game.bowling.repository.GameRepository

class GameService(private val gameRepository: GameRepository, private val frameService: FrameService) {

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
        val frames: List[Frame] = frameService.getFrames(game)
        val lastFrameFromDB: Option[Frame] = frameService.getLastFrame(frames)
        val strike = rollToSave.score.contains(10)
        frameService.insertRoll(lastFrameFromDB, rollToSave, gameId, strike)
        findById(gameId)
      case _ =>
        None
    }
  }

  def calculateScore(id: Int): Option[Int] = {
    val game = findById(id)
    game match {
      case Some(game) =>
        val frames: List[FrameDTO] = convertFrameToDTO(game.frames.get)

        def sumScore(framesLeft: List[FrameDTO], total: Int): Int = {
          var tempTotal = framesLeft.head.sum
          if (framesLeft.tail != Nil && framesLeft.head.sum == 10) {
            tempTotal += framesLeft(1).roll1
            if (framesLeft.head.strike) {
              if (!framesLeft(1).strike) tempTotal += framesLeft(1).roll2
              else tempTotal += framesLeft(2).roll1
            }
          }
          if (framesLeft.tail.nonEmpty && (frames.size - framesLeft.tail.size) < 10) sumScore(framesLeft.tail, total + tempTotal)
          else total + tempTotal
        }

        val score = sumScore(frames, 0)
        Some(score)
      case _ =>
        None
    }
  }

  private def convertFrameToDTO(frames: List[Frame]): List[FrameDTO] =
    frames.map(frame => {
      val rolls = frame.rolls.get
      val firstRoll = rolls.head
      val secondScore = if (frame.strike) 0 else rolls.last.score.get
      FrameDTO(firstRoll.score.get, frame.strike, secondScore)
    })

  case class FrameDTO(roll1: Int, strike: Boolean, roll2: Int) {
    def sum: Int = roll1 + roll2
  }

  def delete(id: Int): Int = {
    gameRepository.delete(id)
  }

}