package com.game.bowling.service

import com.game.bowling.model.{Frame, Game, Roll}
import com.game.bowling.repository.FrameRepository

import scala.math.Ordered.orderingToOrdered

class FrameService() {

  private val frameRepository = new FrameRepository
  private val rollService = new RollService

  def getLastFrame(frames: List[Frame]): Option[Frame] =
    frames.reduceOption((a1, a2) => if (a1.number > a2.number) a1 else a2)

  def createFrame(frame: Frame, gameId: Int): Option[Frame] = {
    frameRepository.save(frame, gameId)
  }

  def getFrames(game: Game): List[Frame] = {
    game.frames match {
      case Some(frameList) => frameList
      case _ => List.empty[Frame]
    }
  }

  private def addRoll(rollToSave: Roll, frame: Frame): Option[Frame] = {
    val nextRollNumber = frame.rolls match {
      case Some(rolls) => rolls.head.number.get + 1
      case None => rollToSave.number.getOrElse(0) + 1
    }

    val nextRoll = Roll(None, None, rollToSave.score, None)
    val createdRoll = rollService.createRoll(nextRoll, nextRollNumber, frame.id.get)
    frameRepository.findById(createdRoll.get.frameId.get)
  }


  def insertRoll(lastFrameFromDB: Option[Frame], rollToSave: Roll, gameId: Int, strike: Boolean): Option[Frame] =
    lastFrameFromDB match {
      case Some(frame) =>
        val frameLastRoll = rollService.getLastRoll(frame.rolls.get)
        if ((frameLastRoll.isDefined && frameLastRoll.get.number.get == 2) || frame.strike) {
          val nextFrameNumber = Some(frame.number.get + 1)
          val nextFrame = Frame(None, nextFrameNumber, strike, Some(List(rollToSave)))
          val createdFrame = createFrame(nextFrame, gameId)
          addRoll(rollToSave, createdFrame.get)
        } else {
          addRoll(rollToSave, frame)
        }
      case _ =>
        val createdFrame = createFrame(Frame(None, Some(1), strike, Some(List(rollToSave))), gameId)
        addRoll(rollToSave, createdFrame.get)
    }

}
