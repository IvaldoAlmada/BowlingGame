package com.game.bowling.service

import com.game.bowling.model.{Frame, Game}
import com.game.bowling.repository.GameRepository

class GameService() {

  private val gameRepository: GameRepository = new GameRepository
  def findById(id: Int): Option[Game] =
    gameRepository.findById(id)

  def save(game: Game): Option[Game] =
    gameRepository.save(game)

  def addRoll(frame: Frame, id: Int): Option[Game] = {
    val game = gameRepository.findById(id)
    game match {
      case Some(game) =>
        val updatedFrames = game.frames.::(frame)
        val gameToUpdate = game.copy(game.id, updatedFrames)
        gameRepository.update(gameToUpdate)
      case _ =>
        None
    }
  }

  def calculateScore(id: Int): Option[Int] = {
    val game = gameRepository.findById(id)
    game match {
      case Some(game) =>
        val score = game.frames.map(frame => frame.score).sum
        Some(score)
      case _ =>
        None
    }
  }

  def delete(id: Int): Option[Boolean] = {
    gameRepository.delete(id)
  }
}
