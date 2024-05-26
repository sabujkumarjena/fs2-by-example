package com.allevite.chapt5_Communication
import cats.effect.{IO, IOApp}
import fs2.*
import fs2.concurrent.Channel

import scala.concurrent.duration.*

object Channels extends IOApp.Simple:
  val s: Stream[IO, Nothing] =
    Stream.eval(Channel.unbounded[IO, Int]).flatMap { channel =>
      val p = Stream.iterate(1)(_ + 1).covary[IO].evalMap(channel.send).drain //producer
      val c = channel.stream.evalMap(i => IO.println(s" Read $i")).drain //consumer
      c.concurrently(p)
    }

  val s2: Stream[IO, Nothing] =
    Stream.eval(Channel.bounded[IO, Int](1)).flatMap { channel =>
      val p = Stream.iterate(1)(_ + 1).covary[IO].evalMap(channel.send).drain //producer
      val c = channel.stream.evalMap(i => IO.println(s" Read $i")).drain //consumer
      c.concurrently(p)
    }

  val s3: Stream[IO, Nothing] =
    Stream.eval(Channel.bounded[IO, Int](1)).flatMap { channel =>
      val p = Stream.iterate(1)(_ + 1).covary[IO].evalMap(channel.send).drain //producer
      val c = channel.stream.metered(200.millis).evalMap(i => IO.println(s" Read $i")).drain //consumer
      c.concurrently(p)
    }
  override def run: IO[Unit] =
    s.interruptAfter(5.seconds).compile.drain
    s2.interruptAfter(5.seconds).compile.drain
    s3.interruptAfter(5.seconds).compile.drain