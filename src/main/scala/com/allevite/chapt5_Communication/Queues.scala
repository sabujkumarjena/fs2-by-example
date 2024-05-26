package com.allevite.chapt5_Communication


import cats.effect.{IO, IOApp, Ref}
import fs2.*

import scala.concurrent.duration.*
import cats.effect.std.Queue
import scala.util.Random

import java.time.LocalDateTime

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

  val q3 = Stream.eval(Queue.unbounded[IO, Option[Int]]).flatMap {q =>
    val p = (Stream.range(0, 10).map(Some.apply) ++ Stream(None) ++ Stream(Some(11))) .evalMap(q.offer)
    val c = Stream.fromQueueNoneTerminated(q).evalMap(i => IO.println(i))
    c.merge(p)
  }

  trait Controller:
    def postAccount(customerId: Long, accType: String, creationDate: LocalDateTime): IO[Unit]

  class Server(controller: Controller):
    def start():IO[Nothing] =
      val prog =
        for
          randomWait <- IO(math.abs(Random.nextInt) % 500)
          _ <- IO.sleep(randomWait.millis)
          _ <- controller.postAccount(
            customerId = Random.between(1L, 1000L),
            accType =  if Random.nextBoolean then "SB" else "CA",
            creationDate = LocalDateTime.now()
          )
        yield ()
      prog.foreverM

  object PrintController extends Controller:
    override def postAccount(customerId: Long, accType: String, creationDate: LocalDateTime): IO[Unit] =
      IO.println(s"Initiating account creation. Customer: $customerId, Account type: $accType Created: $creationDate")

  case class CreateAccountData(customerId: Long, accType: String, creationDate: LocalDateTime)

  class QueueController(queue: Queue[IO,CreateAccountData]) extends  Controller:
    override def postAccount(customerId: Long, accType: String, creationDate: LocalDateTime): IO[Unit] =
      queue.offer(CreateAccountData(customerId,accType, creationDate))

  //Exercise
  // Create a stream that emits the queue
  // Create a stream for the server (started)
  // Create a consumer stream which reads from the queue and prints the message
  // Run everything concurrently

  val myProg =
    Stream.eval(Queue.unbounded[IO, CreateAccountData]).flatMap {q =>
      val serverStream = Stream.eval(new Server(new QueueController(q)).start())
      val consumerStream = Stream.fromQueueUnterminated(q).printlns
      consumerStream.merge(serverStream)
    }
  override def run: IO[Unit] =
    q1.compile.drain
    q2.compile.drain
    q3.interruptAfter(5.seconds).compile.drain
    new Server(PrintController).start()
    myProg.interruptAfter(5.seconds).compile.drain

