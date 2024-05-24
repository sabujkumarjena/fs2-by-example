package com.allevite.chapt2_effectful

import cats.effect.{IO, IOApp}
import fs2.*
object ErrorHandling extends IOApp.Simple:
  val s: Stream[IO, Nothing] = Stream.eval(IO.raiseError(new Exception("boom")))
  val s2: Stream[IO, Nothing] = Stream.raiseError[IO](new Exception("boom2"))
  val s3: Stream[IO, Int] = Stream.repeatEval(IO.println("emiting").as(45)).take(3) ++ Stream.raiseError[IO](new Exception("error after"))

  def dowork(i: Int): Stream[IO, Int] =
    Stream.eval(IO(math.random())).flatMap {flag =>
      if(flag < 0.5) Stream.eval(IO.println(s"Processing $i").as(i))
      else Stream.raiseError[IO](new Exception(s" Error while handling $i"))
    }

  //Exercise
  extension [A](s: Stream[IO, A])
    def flatAttempt: Stream[IO, A] =
//      s.attempt.flatMap :
//        case Right(a) => Stream.emit(a)
//        case Left(e) => Stream.empty
        s.attempt.collect:
          case Right(v) => v

  override def run: IO[Unit] =
    s.compile.drain // throw exception
    s2.compile.drain // throw exception
    s3.compile.drain

    Stream.iterate(1)(_ + 1)
      .flatMap(dowork)
      .take(10).compile.toList.flatMap(IO.println)

    Stream.iterate(1)(_ + 1)
      .flatMap(dowork)
      .take(10)
      .handleErrorWith(e => Stream.exec(IO.println(s" Recovering: ${e.getMessage}")))
      .compile.toList.flatMap(IO.println)

    Stream.iterate(1)(_ + 1)
      .flatMap(dowork)
      .take(10)
      .attempt
      .handleErrorWith(e => Stream.exec(IO.println(s" Recovering: ${e.getMessage}")))
      .compile.toList.flatMap(IO.println)

    Stream.iterate(1)(_ + 1)
      .flatMap(dowork)
      .take(10)
      .flatAttempt
      .handleErrorWith(e => Stream.exec(IO.println(s" Recovering: ${e.getMessage}")))
      .compile.toList.flatMap(IO.println)
