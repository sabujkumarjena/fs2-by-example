package com.allevite.chap4_Concurrency
import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import fs2.*

import scala.concurrent.duration.*

object Join extends IOApp.Simple:
  val fs1 = Stream(1,2,3,4).covary[IO].metered(100.millis)
  val fs2 = Stream(5,6,7,8).covary[IO].metered(50.millis)
  val jfs = Stream(fs1, fs2).parJoinUnbounded //same as fs1.merge(fs2)

  val is1 = Stream.iterate(3000000)(_ +1).covary[IO].metered(50.millis)
  val is2 = Stream.iterate(4000000)(_ +1).covary[IO].metered(50.millis)
  val is3 = Stream.iterate(5000000)(_ +1).covary[IO].metered(50.millis)
  val jis = Stream(fs1, fs2, is1, is2).parJoinUnbounded
  val fs1f = Stream(1,2,3,4).covary[IO].metered(100.millis) ++ Stream.raiseError[IO](new Exception("I am raising error"))
  val jisf = Stream(fs1f, fs2, is1, is2).parJoinUnbounded
  val jb = Stream(fs1, fs2, is1).parJoin(2)
  val jb2= Stream(is1, is2, is3).parJoin(2)
  //Exercise: Multiple producers and multiple consumers
  def producer(id: Int, queue: Queue[IO, Int]): Stream[IO, Nothing] =
    Stream.repeatEval(queue.offer(id)).map(_ => s"Producing message from producer $id").printlns.metered(250.millis)
  def consumer(id: Int, queue: Queue[IO, Int]): Stream[IO, Nothing] =
    Stream.repeatEval(queue.take).map(i => s"Consuming message $i from consumer $id").printlns.metered(250.millis)

  //Create a stream that emits the queue
  //Use that queue to create 5 producers and 10 consumers with sequential ids
  //Run the producers and the consumers in parallel
  // Finish after 5 seconds
  val q: Stream[IO, Nothing] = Stream.eval(Queue.unbounded[IO, Int]).flatMap { queue =>
    val ps = Stream.range(0, 5).map(id => producer(id, queue))
    val cs = Stream.range(0, 10).map(id => consumer(id, queue))
    val all = ps ++ cs
    all.parJoinUnbounded
  }
  override def run: IO[Unit] =
    jfs.printlns.compile.drain
    jis.printlns.interruptAfter(3.seconds).compile.drain
    jisf.printlns.interruptAfter(3.seconds).compile.drain // stream fails
    jb.printlns.interruptAfter(3.seconds).compile.drain
    jb2.printlns.interruptAfter(3.seconds).compile.drain
    q.interruptAfter(3.seconds).compile.drain