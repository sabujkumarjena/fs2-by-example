package com.allevite.chap4_Concurrency

import cats.effect.{IO, IOApp}
import fs2.*

import scala.concurrent.duration.*
import scala.util.Random
object Merge extends IOApp.Simple:
  val i1: Stream[IO, String] = Stream.iterate("0")(_ + "1").covary[IO].metered(100.millis)
  val i2: Stream[IO, String] = Stream.iterate("a")(_ + "a").covary[IO].metered(200.millis)
  val i3: Stream[IO, String] = i1.merge(i2)
  val s1f: Stream[IO, String] = Stream("a", "b", "c").covary[IO].metered(100.millis) ++ Stream.raiseError[IO](new Exception("i am failing"))
  val s2f: Stream[IO, String] = s1f.merge(i2)
  val s3f: Stream[IO, String] = i1.merge(s1f)

  val s1 = Stream(1,2,3,4).covary[IO].metered(100.millis)
  val s2 = Stream(5,6,7,8).covary[IO].metered(100.millis)
  val s3 =  s1.merge(s2)

  //merging finite and infinite stream
  val i4 = s3.merge(i2)
  val s4 = s3.mergeHaltL(i2)
  val s5 = i2.mergeHaltR(s4)
  val s6 = i2.mergeHaltBoth(s4) //halts when either one halts
  //Exercise : Fetching quotes from 2 sources
  def fetchRandomQuotesFromSource1:IO[String] = IO(Random.nextString(5)+"s1")
  def fetchRandomQuotesFromSource2:IO[String] = IO(Random.nextString(15)+ "s2")
  //Fetch 100 quotes from source 1, 150 quotes from source 2, runs for 5 seconds and prints the quotes to console
  val sq1 = Stream.repeatEval(fetchRandomQuotesFromSource1).take(100)
  val sq2 = Stream.repeatEval(fetchRandomQuotesFromSource2).take(150)
  val sq = sq1.merge(sq2)

  override def run: IO[Unit] =
    i3.interruptAfter(5.seconds).printlns.compile.drain
    s2f.interruptAfter(5.seconds).printlns.compile.drain
    s3f.interruptAfter(5.seconds).printlns.compile.drain
    s3.compile.toList.flatMap(IO.println) //List(5, 1, 6, 2, 7, 3, 8, 4)
    i4.interruptAfter(5.seconds).printlns.compile.drain
    s4.compile.toList.flatMap(IO.println)
    s5.compile.toList.flatMap(IO.println) //List(5, a, a, 1, 6, 2, 7, 3, aa, aa, 4, 8)
    s6.compile.toList.flatMap(IO.println)
    sq.interruptAfter(5.seconds).printlns.compile.drain

