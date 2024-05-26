package com.allevite.chap4_Concurrency

import fs2.*
import cats.effect.{IO, IOApp}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.*
import scala.util.Random

object TimedRevisited extends IOApp.Simple:

  val format: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:MM:ss")
  def printNow: IO[Unit] = IO.println(LocalDateTime.now().format(format))
  def randomResult: IO[Int] = IO(Random.between(1,1000))
  def process(executionTime: FiniteDuration): IO[Int] =
    IO.sleep(executionTime) >> randomResult.flatTap(_ => printNow)

  val s: Stream[IO, Int] = Stream.repeatEval(process(1.second))
  val fixedRateStream: Stream[IO, Unit] = Stream.fixedRate[IO](2.seconds)
  //Exercise - Implement metered
  def metered[A](s: Stream[IO, A], d: FiniteDuration): Stream[IO, A] =
    Stream.fixedRate[IO](d).zipRight(s)

  val fixedDelayStream: Stream[IO, Unit] = Stream.fixedDelay[IO](2.seconds)
  //Exercise
  def spaced[A](s: Stream[IO, A], d: FiniteDuration): Stream[IO,A] =
    Stream.fixedDelay[IO](d).zipRight(s)

  val awakeEveryStream: Stream[IO, FiniteDuration] = Stream.awakeEvery[IO](2.seconds)
  val awakeDelayStream: Stream[IO, FiniteDuration] = Stream.awakeDelay[IO](2.seconds)
  override def run: IO[Unit] =
    s.take(3).compile.toList.flatMap(IO.println)
    fixedRateStream.take(3).printlns.compile.drain
    fixedRateStream.zip(s).take(5).compile.toList.flatMap(IO.println)
    metered(s, 3.seconds).take(5).compile.toList.flatMap(IO.println)
    fixedDelayStream.take(5).printlns.compile.drain
    fixedDelayStream.zip(s).take(5).printlns.compile.drain
    spaced(s, 2.seconds).take(3).compile.toList.flatMap(IO.println)
    awakeEveryStream.take(3).compile.toList.flatMap(IO.println)
    awakeEveryStream.zipRight(s).take(3).compile.toList.flatMap(IO.println)