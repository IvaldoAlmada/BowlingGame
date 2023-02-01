package com.game.bowling.service

import com.game.bowling.model.Roll
import com.game.bowling.repository.RollRepository

import scala.math.Ordered.orderingToOrdered

class RollService(private val rollRepository: RollRepository) {

  def getLastRoll(rolls: List[Roll]): Option[Roll] =
    rolls.reduceOption((a1, a2) => if (a1.number > a2.number) a1 else a2)

  def createRoll(roll: Roll, rollNumber: Int, frameId: Int): Option[Roll] =
    rollRepository.save(roll, rollNumber, frameId)
}
