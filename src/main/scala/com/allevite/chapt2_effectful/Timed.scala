package com.allevite.chapt2_effectful

import cats.effect.{IO, IOApp}
import fs2.*
import scala.concurrent.duration.*

object Timed extends IOApp.Simple:

  val drinkWater = Stream.iterateEval(1)(n => (IO.sleep(1500.millis) *> IO.println("Drink more water")).as(n+1))

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