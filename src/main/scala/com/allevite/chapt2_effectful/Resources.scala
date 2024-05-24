package com.allevite.chapt2_effectful

import cats.effect.{IO, IOApp, Resource}
import fs2.*

import java.io.{BufferedReader, FileReader}
object Resources extends IOApp.Simple:

  val acquireReader = IO.println("Acquiring resource..") >> IO.blocking(new BufferedReader(new FileReader("sets.csv")))
  val releaseReader = (reader: BufferedReader) => IO.println("Releasing resource..") *> IO.blocking(reader.close())

  def readLines(reader: BufferedReader): Stream[IO, String] =
    Stream
      .repeatEval(IO.blocking(reader.readLine()))
      .takeWhile(_ != null)
//      Stream.raiseError[IO](new Exception("boom"))

  val readerResource: Resource[IO, BufferedReader] = Resource.make(acquireReader)(releaseReader)

  override def run: IO[Unit] =
    Stream
      .bracket(acquireReader)(releaseReader)
      .flatMap(reader => readLines(reader))
      .take(10)
      .compile
      .toList
      .flatMap(IO.println)

    Stream
      .bracket(acquireReader)(releaseReader)
      .flatMap(reader => readLines(reader))
      .take(10)
      .printlns
      .compile
      .drain

    Stream
      .resource(readerResource)
      .flatMap(reader => readLines(reader))
      .take(10)
      .printlns
      .compile
      .drain

    Stream
      .fromAutoCloseable(acquireReader)
      //.resource(readerResource)
      .flatMap(reader => readLines(reader))
      .take(10)
      .printlns
      .compile
      .drain


