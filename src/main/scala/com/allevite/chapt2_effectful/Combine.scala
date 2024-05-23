package com.allevite.chapt2_effectful

import cats.effect.{IO, IOApp}
import fs2.*

object Combine extends IOApp.Simple:

  //repeatEval
//  val s: Stream[IO, Int] = Stream.repeatEval(IO.println("Emitting...") >> IO(45))
  val s: Stream[IO, Int] = Stream.repeatEval( IO{ println("Emitting") ; 45})

  override def run: IO[Unit] =
    s.compile.drain
