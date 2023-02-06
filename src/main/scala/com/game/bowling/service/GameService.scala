package com.game.bowling.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.game.bowling.model.{Frame, FrameDTO, Game, Roll}
import com.game.bowling.repository.GameRepository

import scala.annotation.tailrec

class GameService(private val gameRepository: GameRepository, private val frameService: FrameService) {

  def findById(id: Int): IO[Option[Game]] = {
    gameRepository.findById(id)
  }

  def save(game: Game): IO[Option[Game]] = {
    gameRepository.save(game)
  }

  def complete(gameId: Int): IO[Int] =
    gameRepository.complete(gameId)

  def roll(rollToSave: Roll, gameId: Int): Option[Game] = {
    val game = findById(gameId).unsafeRunSync()
    game match {
      case Some(game) =>
        val frames: List[Frame] = frameService.getFrames(game)
        val lastFrameFromDB: Option[Frame] = frameService.getLastFrame(frames)
        val strike = rollToSave.score.contains(10)
        frameService.insertRoll(lastFrameFromDB, rollToSave, gameId, strike)
        findById(gameId).unsafeRunSync()
      case _ =>
        None
    }
  }

  def calculateScore(id: Int): IO[Option[Int]] = {
    for {
      game <- findById(id)
      gameScore <- game match {
        case Some(game) =>
          val frames: List[FrameDTO] = convertFrameToDTO(game.frames.get)
          val score = sumScore(frames, frames.size, 0)
          IO(score)
        case _ =>
          IO(None)
      }
    } yield gameScore
  }

  @tailrec
  private def sumScore(frames: List[FrameDTO], framesSize: Int, total: Int): Option[Int] = {
    val tempTotal = frames.headOption.getOrElse(FrameDTO(0, strike = false, 0)).sum
    val frameResult = if (frames.nonEmpty && frames.tail != Nil && frames.head.sum == 10) {
      val strikeResult: Int = getStrikeResult(frames)
      frames(1).roll1 + strikeResult
    } else 0
    if (frames.nonEmpty && frames.tail.nonEmpty && (framesSize - frames.tail.size) < 10) sumScore(frames.tail, framesSize, total + tempTotal + frameResult)
    else Some(total + tempTotal)
  }

  private def getStrikeResult(framesLeft: List[FrameDTO]): Int = {
    if (framesLeft.head.strike) {
      if (framesLeft.size > 2) {
        if (framesLeft(1).strike) framesLeft(2).roll1
        else framesLeft(1).roll2
      } else {
        if (framesLeft(1).strike) framesLeft(1).roll1 * 3
        else framesLeft(1).roll1 + framesLeft(1).roll2
      }
    } else {
      0
    }
  }

  private def convertFrameToDTO(frames: List[Frame]): List[FrameDTO] =
    frames.map(frame => {
      val rolls = frame.rolls.get
      val firstRoll = rolls.head
      val secondScore = if (frame.strike) 0 else rolls.last.score.get
      FrameDTO(firstRoll.score.get, frame.strike, secondScore)
    })

  def delete(id: Int): IO[Int] = {
    gameRepository.delete(id)
  }
}