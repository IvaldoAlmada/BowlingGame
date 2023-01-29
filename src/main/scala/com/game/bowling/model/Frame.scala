package com.game.bowling.model

case class Frame(id: Option[Int], number: Option[Int], strike: Boolean, rolls: Option[List[Roll]])
