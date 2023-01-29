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
    val gameToReturn = Game(Some(1), Some("12345"), Some(List(frame)))

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

    val frame1 = Frame(id = Some(1), number = Some(1), strike = false, rolls = Some(List(roll1, roll2)))
    val frame2 = Frame(id = Some(2), number = Some(2), strike = true, rolls = Some(List(roll3)))
    val frame3 = Frame(id = Some(3), number = Some(3), strike = false, rolls = Some(List(roll4, roll5)))

    val game = Game(id = Some(gameId), name = Some("to calculate score"), frames = Some(List(frame1, frame2, frame3)))

    when(gameRepository.findById(gameId)).thenReturn(Some(game))
    assertResult(44) {
      gameService.calculateScore(gameId).get
    }
  }
}
