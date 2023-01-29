package com.game.bowling.model

case class Game(id: Option[Int], name: Option[String], complete: Boolean, frames: Option[List[Frame]])

