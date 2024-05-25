package com.allevite.chap4_Concurrency
import cats.effect.{IO, IOApp}
import fs2.*
import scala.concurrent.duration.*

object Join extends IOApp.Simple:
  val fs1 = Stream(1,2,3,4).covary[IO].metered(100.millis)
  val fs2 = Stream(5,6,7,8).covary[IO].metered(50.millis)
  val jfs = Stream(fs1, fs2).parJoinUnbounded //same as fs1.merge(fs2)

  val is1 = Stream.iterate(3000000)(_ +1).covary[IO].metered(50.millis)
  val is2 = Stream.iterate(4000000)(_ +1).covary[IO].metered(50.millis)
  val jis = Stream(fs1, fs2, is1, is2).parJoinUnbounded
  val fs1f = Stream(1,2,3,4).covary[IO].metered(100.millis) ++ Stream.raiseError[IO](new Exception("I am raising error"))
  val jisf = Stream(fs1f, fs2, is1, is2).parJoinUnbounded
  override def run: IO[Unit] =
    jfs.printlns.compile.drain
    jis.printlns.interruptAfter(3.seconds).compile.drain
    jisf.printlns.interruptAfter(3.seconds).compile.drain // stream fails