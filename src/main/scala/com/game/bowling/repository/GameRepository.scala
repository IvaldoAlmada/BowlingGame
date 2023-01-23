package com.game.bowling.repository

import com.game.bowling.model.{Frame, Game}

class GameRepository() {

  private val frame1List =List(Frame(1, 7, 1))
  
  private val game1 = Game(1, frame1List)

  private val games: Map[Int, Game] = Map(game1.id -> game1)

  def findById(id: Int): Option[Game] =
    games.get(id)

}
