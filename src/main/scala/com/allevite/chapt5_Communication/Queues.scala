package com.allevite.chapt5_Communication


import cats.effect.{IO, IOApp, Ref}
import fs2.*

import scala.concurrent.duration.*
import cats.effect.std.*

object Queues extends IOApp.Simple :

  val q1: Stream[IO, Unit] = Stream.eval(Queue.unbounded[IO, Int]).flatMap { queue =>
    Stream.eval(Ref.of[IO, Int](0)).flatMap { ref =>
      val p = Stream.iterate(0)(_ + 1).covary[IO]
        .evalMap(i => IO.println(f"Offering $i") >> queue.offer(i))
        .drain //producer
      val c = Stream.fromQueueUnterminated(queue)
        .evalMap(i => ref.update(_ + i))
        .drain //consumer
      p.merge(c).interruptAfter(3.seconds) ++ Stream.eval(ref.get.flatMap(IO.println))
    }
  }
  val q2: Stream[IO, Unit] = Stream.eval(Queue.unbounded[IO, Int]).flatMap { queue =>
    Stream.eval(Ref.of[IO, Int](0)).flatMap { ref =>
      val p = Stream.iterate(0)(_ + 1).covary[IO]
        .evalMap(i => IO.println(f"Offering $i") >> queue.offer(i))
        .drain //producer
      val c = Stream.fromQueueUnterminated(queue)
        .evalMap(i => ref.update(_ + i))
        .metered(300.millis)
        .drain //consumer
      p.merge(c).interruptAfter(3.seconds) ++ Stream.eval(ref.get.flatMap(IO.println))
    }
  }
  override def run: IO[Unit] =
    q1.compile.drain
    q2.compile.drain
