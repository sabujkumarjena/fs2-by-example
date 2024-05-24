package com.allevite.chapt2_effectful

import cats.effect.{IO, IOApp}
import fs2.*
import scala.concurrent.duration.*

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

  val s6: Stream[IO, Int] = Stream.exec(IO.println("start")) ++ Stream(1,2,3) ++ Stream(4,5,6) ++ Stream.exec(IO.println("Finish"))

  val delayed: Stream[IO, Unit] = Stream.sleep_[IO](3.second) ++ Stream.eval(IO.println(" I am awake"))

  //Exercise
  def evalEvery[A](d: FiniteDuration)(fa: IO[A]): Stream[IO, A] =
    (Stream.sleep_[IO](d) ++ Stream.eval(fa)).repeat




  override def run: IO[Unit] =
    s.compile.drain
    s2.compile.drain
    s3.compile.toList.flatMap(IO.println)
    s4.compile.toList.flatMap(IO.println)
    s5.compile.toList.flatMap(IO.println)
    s6.compile.toList.flatMap(IO.println)
    delayed.compile.drain

    evalEvery(2.seconds)(IO.println("Hi").as(42)).take(5).compile.toList.flatMap(IO.println)
