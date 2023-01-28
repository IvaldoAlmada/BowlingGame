package com.game.bowling.service

import com.game.bowling.model.{Frame, Game, Roll}
import org.scalatest.funsuite.AnyFunSuite

class GameServiceTest extends AnyFunSuite {

  val gameService = new GameService

  test("create game") {
    val roll = Roll(Some(1), Some(1), Some(7))
    val frame = Frame(Some(1), Some(1), Some(List(roll)))
    val gameToReturn = Game(Some(1), Some("12345"), Some(List(frame)))
    assert(gameService.findById(1).contains(gameToReturn))
  }

}
