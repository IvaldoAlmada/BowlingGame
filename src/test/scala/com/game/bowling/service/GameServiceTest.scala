package com.game.bowling.service

import com.game.bowling.model.{Frame, Game, Row}
import org.scalatest.funsuite.AnyFunSuite

class GameServiceTest extends AnyFunSuite {

  val gameService = new GameService

  test("create game") {
    val gameToReturn = Game(1, List(Frame(1, List(Row(1, 7)))))
    assert(gameService.findById(1).contains(gameToReturn))
  }

}
