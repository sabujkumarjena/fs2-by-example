package com.allevite.chap4_Concurrency

import fs2.*
import cats.effect.{IO, IOApp}

import java.time.LocalDateTime
import scala.concurrent.duration.*

object Zip extends IOApp.Simple:
  val s1: Stream[IO, Int] = Stream(1, 2, 3, 4).covary[IO].metered(1.second)
  val s2: Stream[IO, Int] = Stream(5, 6, 7, 8).covary[IO].metered(100.millis)
  val s3: Stream[IO, Int] = Stream(5, 6, 7, 8, 9, 10).covary[IO].metered(100.millis)
  val i1: Stream[IO, Int] = Stream.iterate(0)(_ + 1).covary[IO].metered(200.millis)
  val s1f: Stream[IO, Int] = s1 ++ Stream.raiseError[IO](new Exception("s1 is failing"))
  val s3f: Stream[IO, Int] = s3 ++ Stream.raiseError[IO](new Exception("s3 is failing"))
  val i2: Stream[IO, LocalDateTime] = Stream.repeatEval(IO(LocalDateTime.now)).evalTap(IO.println).metered(1.second)
  val i4: Stream[IO, String] = Stream.repeatEval(IO.println("Pulling left").as("L"))
  val i5: Stream[IO, String] = Stream.repeatEval(IO.println("Pulling right").as("R"))
  val i6: Stream[IO, Int] = Stream.iterateEval(0)(i => IO.println(s"Pulling left $i").as(i + 1))
  val i7: Stream[IO, Int] = Stream.iterateEval(0)(i => IO.println(s"Pulling right $i").as(i + 1))
  override def run: IO[Unit] =
    s1.zip(s2).printlns.compile.drain
    s1.zip(s3).printlns.compile.drain
    s1.zip(i1).printlns.compile.drain
    s1f.zip(s3).printlns.compile.drain
    s1.zip(s3f).printlns.compile.drain // no error
    i2.zip(i1).interruptAfter(3.seconds).compile.toList.flatMap(IO.println)
    i2.zipRight(i1).interruptAfter(3.seconds).compile.toList.flatMap(IO.println)
    i4.zip(i5).take(15).compile.toList.flatMap(IO.println)
    i4.parZip(i5).take(15).compile.toList.flatMap(IO.println)
    i6.parZip(i7).take(15).compile.toList.flatMap(IO.println)
