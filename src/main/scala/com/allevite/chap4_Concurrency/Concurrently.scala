package com.allevite.chap4_Concurrency
import fs2.*
import cats.effect.{IO, IOApp, Ref}
import scala.concurrent.duration.*

import scala.concurrent.duration.*
object Concurrently extends IOApp.Simple:
  val s1: Stream[IO, Nothing] = Stream(1, 2, 3, 4).covary[IO].printlns
  val s2: Stream[IO, Nothing] = Stream(5, 6, 7, 8).covary[IO].printlns
  /**
   * Runs the supplied stream in the background as elements from this stream are pulled.
   * The resulting stream terminates upon termination of this stream. The background stream will be
   * interrupted at that point. Early termination of that does not terminate the resulting stream.
   * Any errors that occur in either this or that stream result in the overall stream terminating with an error.
   * Upon finalization, the resulting stream will interrupt the background stream and wait for it to be finalized.
   * This method is equivalent to this mergeHaltL that. drain, just more efficient for this and that evaluation.
   */
  val sc: Stream[IO, Nothing] = s1.concurrently(s2)

  val is1 = Stream.iterate(0)(_+1).covary[IO].printlns
  val sc2 = is1.concurrently(s2).interruptAfter(3.seconds)
  val is2 = Stream.iterate(2000)(_+1).covary[IO].printlns
  val sc3 = s1.concurrently(is2)
  val s1Failing = Stream.repeatEval(IO(45)).take(500).printlns ++ Stream.raiseError[IO](new Exception(" s1 failing"))
  val s2Failing = Stream.repeatEval(IO(6000)).take(500).printlns ++ Stream.raiseError[IO](new Exception(" s2 failing"))
  val is3 = Stream.iterate(30000)(_+1).covary[IO]
  val is4 = Stream.iterate(40000)(_+1).covary[IO]
  //Exercise- Progress tracker
  val numItems = 30
  def processor(itemProcessed: Ref[IO, Int]): Stream[IO, Unit] =
    Stream
      .repeatEval(itemProcessed.update(_+1))
      .take(numItems)
      .metered(100.millis)
      .drain
  def progressTracker(itemProcessed: Ref[IO, Int]) =
    Stream
      .repeatEval(itemProcessed.get.flatMap(n => IO.println(s"Progress: ${n * 100/ numItems} %")))
      .metered(100.millis)
      .drain
  // Create a stream that emits a ref (initially 0)
  // Run the processor and the progressTracker concurrently
  val ts = Stream.eval(Ref.of[IO, Int](0)).flatMap { itemsProcessed =>
    processor(itemsProcessed). concurrently(progressTracker(itemsProcessed))
  }

  override def run: IO[Unit] =
    sc.compile.drain
    sc2.compile.drain
    sc3.compile.drain
    is1.concurrently(s2Failing).compile.drain //fail
    s1Failing.concurrently(is1).compile.drain //fail
    is3.concurrently(is4).take(100).compile.toList.flatMap(IO.println) //only results from 1st stream
    is3.merge(is4).take(100).compile.toList.flatMap(IO.println) // from both stream
    Stream(is3, is4).parJoinUnbounded.take(100).compile.toList.flatMap(IO.println) //from both stream
    ts.compile.drain
