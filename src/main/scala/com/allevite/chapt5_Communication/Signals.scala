package com.allevite.chapt5_Communication
import fs2.*
import cats.effect.{IO, IOApp}
import fs2.concurrent.SignallingRef
import scala.concurrent.duration.*
import scala.util.Random

object Signals extends IOApp.Simple:
  def signaller(signal: SignallingRef[IO, Boolean]): Stream[IO, Nothing] =
    Stream
      .repeatEval(IO(Random.between(1, 1000)).flatTap(i => IO.println(s"Generating $i")))
      .metered(100.millis)
      .evalMap(i => if i % 5 == 0 then signal.set(true) else IO.unit)
      .drain
  def worker(signal: SignallingRef[IO, Boolean]) : Stream[IO, Nothing] =
    Stream
      .repeatEval(IO.println("Working..."))
      .metered(50.millis)
      .interruptWhen(signal)
      .drain
  override def run: IO[Unit] =
    Stream.eval(SignallingRef[IO, Boolean](false)).flatMap { signal =>
      worker(signal).concurrently(signaller(signal))
    }.compile.drain

