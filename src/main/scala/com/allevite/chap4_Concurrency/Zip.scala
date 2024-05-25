package com.allevite.chap4_Concurrency

import fs2.*
import cats.effect.{IO, IOApp}

import scala.concurrent.duration.*

object Zip extends IOApp.Simple:
  val s1: Stream[IO, Int] = Stream(1, 2, 3, 4).covary[IO].metered(1.second)
  val s2: Stream[IO, Int] = Stream(5, 6, 7, 8).covary[IO].metered(100.millis)
  val s3: Stream[IO, Int] = Stream(5, 6, 7, 8, 9, 10).covary[IO].metered(100.millis)
  val i1: Stream[IO, Int] = Stream.iterate(0)(_ + 1).covary[IO].metered(200.millis)
  val s1f: Stream[IO, Int] = s1 ++ Stream.raiseError[IO](new Exception("s1 is failing"))
  val s3f: Stream[IO, Int] = s3 ++ Stream.raiseError[IO](new Exception("s3 is failing"))
  override def run: IO[Unit] =
    s1.zip(s2).printlns.compile.drain
    s1.zip(s3).printlns.compile.drain
    s1.zip(i1).printlns.compile.drain
    s1f.zip(s3).printlns.compile.drain
    s1.zip(s3f).printlns.compile.drain // no error

