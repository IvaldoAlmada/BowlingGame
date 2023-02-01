package com.game.bowling.service

import com.game.bowling.model.{Frame, Game, Roll}
import com.game.bowling.repository.GameRepository
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock

class GameServiceTest extends AnyFlatSpec with Matchers {

  val frameService: FrameService = mock[FrameService]
  val gameRepository: GameRepository = mock[GameRepository]
  val gameService: GameService = new GameService(gameRepository, frameService)

  it should "return game by id" in {
    val idToFind = 1
    val roll = Roll(Some(1), Some(1), Some(7), Some(1))
    val frame = Frame(Some(1), Some(1), strike = false, Some(List(roll)))
    val gameToReturn = Game(Some(1), Some("12345"), complete = false, Some(List(frame)))

    when(gameRepository.findById(idToFind)).thenReturn(Some(gameToReturn))
    assert(gameService.findById(idToFind).contains(gameToReturn))
  }

  it should "calculate the game score" in {
    val gameId = 1
    val roll1 = Roll(id = Some(1), number = Some(1), score = Some(7), frameId = Some(1))
    val roll2 = Roll(id = Some(2), number = Some(2), score = Some(3), frameId = Some(1))

    val roll3 = Roll(id = Some(3), number = Some(1), score = Some(10), frameId = Some(2))

    val roll4 = Roll(id = Some(4), number = Some(1), score = Some(1), frameId = Some(3))
    val roll5 = Roll(id = Some(5), number = Some(2), score = Some(6), frameId = Some(3))

    val roll6 = Roll(id = Some(6), number = Some(1), score = Some(2), frameId = Some(4))
    val roll7 = Roll(id = Some(7), number = Some(2), score = Some(8), frameId = Some(4))

    val frame1 = Frame(id = Some(1), number = Some(1), strike = false, rolls = Some(List(roll1, roll2)))
    val frame2 = Frame(id = Some(2), number = Some(2), strike = true, rolls = Some(List(roll3)))
    val frame3 = Frame(id = Some(3), number = Some(3), strike = false, rolls = Some(List(roll4, roll5)))
    val frame4 = Frame(id = Some(4), number = Some(4), strike = false, rolls = Some(List(roll6, roll7)))

    val game = Game(id = Some(gameId), name = Some("to calculate score"), complete = false, frames = Some(List(frame1, frame2, frame3, frame4)))

    when(gameRepository.findById(gameId)).thenReturn(Some(game))
    assertResult(54si) {
      gameService.calculateScore(gameId).get
    }
  }

  it should "calculate the game score with ten strikes" in {
    val gameId = 1
    val roll1 = Roll(id = Some(1), number = Some(1), score = Some(10), frameId = Some(1))
    val roll2 = Roll(id = Some(2), number = Some(1), score = Some(10), frameId = Some(2))
    val roll3 = Roll(id = Some(3), number = Some(1), score = Some(10), frameId = Some(3))
    val roll4 = Roll(id = Some(4), number = Some(1), score = Some(10), frameId = Some(4))
    val roll5 = Roll(id = Some(5), number = Some(1), score = Some(10), frameId = Some(5))
    val roll6 = Roll(id = Some(6), number = Some(1), score = Some(10), frameId = Some(6))
    val roll7 = Roll(id = Some(7), number = Some(1), score = Some(10), frameId = Some(7))
    val roll8 = Roll(id = Some(8), number = Some(1), score = Some(10), frameId = Some(8))
    val roll9 = Roll(id = Some(9), number = Some(1), score = Some(10), frameId = Some(9))
    val roll10 = Roll(id = Some(10), number = Some(1), score = Some(10), frameId = Some(10))

    val frame1 = Frame(id = Some(1), number = Some(1), strike = true, rolls = Some(List(roll1)))
    val frame2 = Frame(id = Some(2), number = Some(2), strike = true, rolls = Some(List(roll2)))
    val frame3 = Frame(id = Some(3), number = Some(3), strike = true, rolls = Some(List(roll3)))
    val frame4 = Frame(id = Some(4), number = Some(4), strike = true, rolls = Some(List(roll4)))
    val frame5 = Frame(id = Some(5), number = Some(5), strike = true, rolls = Some(List(roll5)))
    val frame6 = Frame(id = Some(6), number = Some(6), strike = true, rolls = Some(List(roll6)))
    val frame7 = Frame(id = Some(7), number = Some(7), strike = true, rolls = Some(List(roll7)))
    val frame8 = Frame(id = Some(8), number = Some(8), strike = true, rolls = Some(List(roll8)))
    val frame9 = Frame(id = Some(9), number = Some(9), strike = true, rolls = Some(List(roll9)))
    val frame10 = Frame(id = Some(10), number = Some(10), strike = true, rolls = Some(List(roll10)))

    val game = Game(id = Some(gameId), name = Some("to calculate score"), complete = true, frames = Some(List(frame1, frame2,
      frame3, frame4, frame5, frame6, frame7, frame8, frame9, frame10)))

    when(gameRepository.findById(gameId)).thenReturn(Some(game))
    assertResult(300) {
      gameService.calculateScore(gameId).get
    }
  }

  it should "calculate the game score with nine strikes" in {
    val gameId = 1
    val roll1 = Roll(id = Some(1), number = Some(1), score = Some(10), frameId = Some(1))
    val roll2 = Roll(id = Some(2), number = Some(1), score = Some(10), frameId = Some(2))
    val roll3 = Roll(id = Some(3), number = Some(1), score = Some(10), frameId = Some(3))
    val roll4 = Roll(id = Some(4), number = Some(1), score = Some(10), frameId = Some(4))
    val roll5 = Roll(id = Some(5), number = Some(1), score = Some(10), frameId = Some(5))
    val roll6 = Roll(id = Some(6), number = Some(1), score = Some(10), frameId = Some(6))
    val roll7 = Roll(id = Some(7), number = Some(1), score = Some(10), frameId = Some(7))
    val roll8 = Roll(id = Some(8), number = Some(1), score = Some(10), frameId = Some(8))
    val roll9 = Roll(id = Some(9), number = Some(1), score = Some(10), frameId = Some(9))
    val roll10 = Roll(id = Some(10), number = Some(1), score = Some(8), frameId = Some(10))
    val roll11 = Roll(id = Some(11), number = Some(2), score = Some(2), frameId = Some(10))

    val frame1 = Frame(id = Some(1), number = Some(1), strike = true, rolls = Some(List(roll1)))
    val frame2 = Frame(id = Some(2), number = Some(2), strike = true, rolls = Some(List(roll2)))
    val frame3 = Frame(id = Some(3), number = Some(3), strike = true, rolls = Some(List(roll3)))
    val frame4 = Frame(id = Some(4), number = Some(4), strike = true, rolls = Some(List(roll4)))
    val frame5 = Frame(id = Some(5), number = Some(5), strike = true, rolls = Some(List(roll5)))
    val frame6 = Frame(id = Some(6), number = Some(6), strike = true, rolls = Some(List(roll6)))
    val frame7 = Frame(id = Some(7), number = Some(7), strike = true, rolls = Some(List(roll7)))
    val frame8 = Frame(id = Some(8), number = Some(8), strike = true, rolls = Some(List(roll8)))
    val frame9 = Frame(id = Some(9), number = Some(9), strike = true, rolls = Some(List(roll9)))
    val frame10 = Frame(id = Some(10), number = Some(10), strike = false, rolls = Some(List(roll10, roll11)))

    val game = Game(id = Some(gameId), name = Some("to calculate score"), complete = true, frames = Some(List(frame1, frame2,
      frame3, frame4, frame5, frame6, frame7, frame8, frame9, frame10)))

    when(gameRepository.findById(gameId)).thenReturn(Some(game))
    assertResult(276) {
      gameService.calculateScore(gameId).get
    }
  }
}

