package com.game.bowling.model

case class Frame(id: Option[Int], number: Option[Int], strike: Boolean, rolls: Option[List[Roll]])

case class FrameDTO(roll1: Int, strike: Boolean, roll2: Int) {
  def sum: Int = roll1 + roll2
}
