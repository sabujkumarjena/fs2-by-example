package com.allevite.chapt2_effectful

import cats.effect.{IO, IOApp}
import fs2.*

object Combine extends IOApp.Simple:

  //repeatEval
//  val s: Stream[IO, Int] = Stream.repeatEval(IO.println("Emitting...") >> IO(45))
  val s: Stream[IO, Int] = Stream.repeatEval( IO{ println("Emitting") ; 45})

  // flatMap
  val s2: Stream[IO, Int] =
    for
      x <- Stream.eval(IO.println("Producing 42") >> IO(42))
      y <- Stream.eval(IO.println("Producing 43") >> IO(x + 1))
    yield y
  //evalMap
  val s3: Stream[IO, Unit] = Stream(1,2,3).evalMap(i => IO.println(s"Element: $i").as(i)) //seems type checking fails
  val myIO: IO[Int] = IO(42).flatTap(IO.println)

  val s4: Stream[IO, Int] = Stream(1,2,3).evalTap(IO.println)

  val s5: Stream[IO, Int] =Stream.range(1, 100).evalFilter(_  => IO(math.random() < 0.5))


  override def run: IO[Unit] =
    s.compile.drain
    s2.compile.drain
    s3.compile.toList.flatMap(IO.println)
    s4.compile.toList.flatMap(IO.println)
    s5.compile.toList.flatMap(IO.println)
