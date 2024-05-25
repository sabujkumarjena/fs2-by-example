package com.allevite.chapt3_pipelines
import cats.effect.{IO, IOApp}
import fs2.*

/**
 * Pull
 *
 * A purely functional data structure that describes a process. This process may evaluate actions in an effect type F,
 * emit any number of output values of type O (or None), and may
 * a) terminate with a single result of type R; or
 * b) terminate abnormally by raising (inside the effect F) an exception, or
 * c) terminate because it was cancelled by another process, or
 * d) not terminate.
 */
object Pulls extends IOApp.Simple:
  val s: Stream[Pure, Int] = Stream(1, 2) ++ Stream(3) ++ Stream(4, 5)
  val outputPull: Pull[Pure, Int, Unit] = Pull.output1(1)
  val outputPull2: Pull[Pure, Int, Unit] = Pull.output(Chunk(1,2,3))
  val donePull: Pull[Pure, Nothing, Unit] = Pull.done // used for signalling that u have finished your computation
  val puePull: Pull[Pure, Nothing, Int] = Pull.pure(5) //Creates an pull that performs no effects, emits no outputs, and terminates successfully with the supplied value as its result.
  val combined: Pull[Pure, Int, Unit] =
    for
      _  <- outputPull
      _  <- outputPull2
    yield ()
  val toPull: Stream.ToPull[Pure, Int] = s.pull
  val echoPull: Pull[Pure, Int, Unit] = s.pull.echo
  val takePull: Pull[Pure, Int, Option[Stream[Pure, Int]]] = s.pull.take(3)
  val dropPull: Pull[Pure, Nothing, Option[Stream[Pure, Int]]] = s.pull.drop(3)

  //Exercise - implement using pulls
  def skipLimit[A](skip: Int, limit: Int)(s: Stream[IO, A]): Stream[IO, A] =
    val p =
      for
        tailOpt <- s.pull.drop(skip)
        _   <- tailOpt match
          case Some(rest) => rest.pull.take(limit)
          case None => Pull.done
      yield ()
    p.stream

  // Waits for a chunk of elements to be available in the source stream. The non-empty chunk of elements
  // along with a new stream are provided as the resource of the returned pull. The new stream can be used
  // for subsequent operations, like awaiting again. A None is returned as the resource of the pull
  // upon reaching the end of the stream.
  val unconsedRange: Pull[Pure, Nothing, Option[(Chunk[Int], Stream[Pure, Int])]] = s.pull.uncons

  def firstChunk[A](s: Stream[Pure, A]): Stream[Pure, A] =
    s.pull.uncons.flatMap {
      case Some((chunk, restOfStream)) => Pull.output(chunk)
      case None   => Pull.done
    }
      .stream

  def firstChunkV2[A]: Stream[Pure, A] =>  Stream[Pure, A] = s =>
    s.pull.uncons.flatMap {
        case Some((chunk, restOfStream)) => Pull.output(chunk)
        case None => Pull.done
      }
      .stream

  def firstChunkV3[A]: Pipe[Pure, A, A] = s =>
    s.pull.uncons.flatMap {
        case Some((chunk, restOfStream)) => Pull.output(chunk)
        case None => Pull.done
      }
      .stream


  override def run: IO[Unit] =
    IO.println(outputPull.stream.toList)
    IO.println(outputPull2.stream.toList)
    IO.println(combined.stream.toList)
    skipLimit(10,10)(Stream.range(1, 100)).compile.toList.flatMap(IO.println)
    IO.println(firstChunk(s).toList)
    IO.println(s.through(firstChunkV2).toList)
