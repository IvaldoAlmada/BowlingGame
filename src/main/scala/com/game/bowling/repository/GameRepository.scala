package com.game.bowling.repository

import com.game.bowling.model.{Frame, Game}

class GameRepository() {

  private val frame1List = List(Frame(id = 1, score = 7, row = 1))

  private val game1 = Game(1, frame1List)

  private var games: Map[Int, Game] = Map(game1.id -> game1)

  def findById(id: Int): Option[Game] =
    games.get(id)

  def save(game: Game): Some[Game] = {
    val increasedGames: Map[Int, Game] = games + (game.id -> game)
    games = increasedGames
    Some(game)
  }

  def delete(id: Int): Option[Boolean] = {
    val listWithoutValue = games - id
    val removedGame = Some(listWithoutValue.size > games.size)
    games = listWithoutValue
    removedGame
  }

  def update(game: Game): Option[Game] = {
    Some(game)
  }

}
