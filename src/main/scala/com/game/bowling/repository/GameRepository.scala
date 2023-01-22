package com.game.bowling.repository

import com.game.bowling.model.Game

class GameRepository() {

  private val game1 = Game(1, 35)

  private val games: Map[Int, Game] = Map(game1.id -> game1)

  def findById(id: Int): Option[Game] =
    games.get(id)

}
