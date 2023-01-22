package com.game.bowling.service

import com.game.bowling.model.Game
import com.game.bowling.repository.GameRepository

class GameService() {

  private val gameRepository: GameRepository = new GameRepository
  def findById(id: Int): Option[Game] =
    gameRepository.findById(id)

}
