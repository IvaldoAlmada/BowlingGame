package com.game.bowling.service

import com.game.bowling.model.{Frame, Game, Roll}
import com.game.bowling.repository.GameRepository

class GameService() {

  private val gameRepository = new GameRepository
  private val frameService = new FrameService

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
