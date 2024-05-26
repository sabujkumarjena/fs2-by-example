package com.allevite.chapt5_Communication
import fs2.*
import cats.effect.{IO, IOApp}
import fs2.concurrent.Topic

import scala.concurrent.duration.*
import scala.util.Random
object Topics extends IOApp.Simple:
  val t1 = Stream.eval(Topic[IO, Int]).flatMap { topic =>
    val p = Stream.iterate(1)(_ +1).covary[IO].evalTap(i => IO.println(f"Writing $i")).metered(200.millis).through(topic.publish).drain //producer
    val c1 = topic.subscribe(10).evalMap(i => IO.println(s"Read $i from c1")).metered(500.millis).drain
    val c2 = topic.subscribe(10).evalMap(i => IO.println(s"Read $i from c2")).drain
    Stream(p, c1, c2).parJoinUnbounded
  }
  override def run: IO[Unit] =
    t1.interruptAfter(3.seconds).compile.drain
