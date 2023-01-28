package com.game.bowling.service

import cats.effect.IO
import com.game.bowling.model.{Frame, Game}
import com.game.bowling.repository.GameRepository

class GameService() {

  private val gameRepository: GameRepository = new GameRepository
  def findById(id: Int): Option[Game] = {
    gameRepository.findById(id)
  }

  def save(game: Game): Option[Game] = {
    gameRepository.save(game)
    Some(game)
  }

  def addRoll(frame: Frame, id: Int): IO[Int] = {
    val game = findById(id)
    game match {
      case Some(game) =>
        val updatedFrames = game.frames
        val gameToUpdate = game.copy(game.id, game.name, updatedFrames)
        gameRepository.update(gameToUpdate)
      case _ =>
        IO.never
    }
  }

  def calculateScore(id: Int): Option[Int] = {
    val game = findById(id)
    game match {
      case Some(game) =>
        val score = game.frames.get.flatMap(frame => frame.rows.get).map(row => row.score.get).sum
        Some(score)
      case _ =>
        None
    }
  }

  def delete(id: Int): IO[Int] = {
    gameRepository.delete(id)
  }
}
