package com.allevite.chapt2_effectful

import cats.effect.{IO, IOApp}
import fs2.*
import scala.concurrent.duration.*

object Timed extends IOApp.Simple:

  val drinkWater = Stream.iterateEval(1)(n => (IO.sleep(1500.millis) *> IO.println("Drink more water")).as(n+1))

  // Debounce
  val resizeEvents: Stream[IO, (Int, Int)] = Stream.iterate((0,0)) { case (w, h) => (w +1 , h +1)}.covary[IO]

  override def run: IO[Unit] =
    drinkWater.compile.drain
    drinkWater.timeout(5.second).compile.drain
    drinkWater.interruptAfter(5.second).compile.drain
    drinkWater.delayBy(1.seconds).interruptAfter(7.seconds).compile.drain

    //Throttleing
    drinkWater
      .metered(1.second)
      .interruptAfter(7.seconds)
      .compile
      .drain
    drinkWater
      .meteredStartImmediately(1.second)
      .interruptAfter(7.seconds)
      .compile
      .drain
    resizeEvents
      .evalMap { case (h, w) => IO.println(s"Resizing window to height $h and width $w")}
      .interruptAfter(5.seconds)
      .compile
      .drain

    resizeEvents
      .debounce(200.millis)
      .evalMap { case (h, w) => IO.println(s"Resizing window to height $h and width $w") }
      .interruptAfter(5.seconds)
      .compile
      .drain

    resizeEvents
      .debounce(200.millis)
      .evalTap { case (h, w) => IO.println(s"Resizing window to height $h and width $w") }
      .interruptAfter(3.seconds)
      .compile
      .toList
      .flatMap(IO.println)