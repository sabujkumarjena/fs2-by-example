package com.allevite

import cats.effect.{IO, IOApp}
import fs2.io.file.{Files, Path}
import fs2.text

//import fs2.*
//import fs2.io.*

import scala.io.Source
import scala.util.{Try, Using}
import scala.concurrent.duration.*

object CSVFileProcessing extends IOApp.Simple :
  case class MySet(id: String, name: String, year: Int, themeId: Int, numParts: Int)

  def parseLine(line: String): Option[MySet] =
    val splitted = line.split(",")
    Try(MySet(
      splitted(0),
      splitted(1),
      splitted(2).toInt,
      splitted(3).toInt,
      splitted(4).toInt
    )).toOption

  def readSets(filename: String, predicate: MySet => Boolean = _ => true, limit: Int= 10): List[MySet] =
    Using(Source.fromFile(filename)) { source =>
      source
        .getLines()
        .flatMap(parseLine)
        .filter(predicate)
        .take(limit)
        .toList
    }.get

  def readSetsAsStream(filename: String, predicate: MySet => Boolean = _ => true, limit: Int= 10): IO[List[MySet]] =
    Files[IO]
      .readAll(Path(filename))
      .through(text.utf8.decode)
      .through(text.lines)
      .map(parseLine)
      .unNone
      .filter(predicate)
      .take(limit) // Stream[IO, MySet]
      .compile
      .toList

  def readSetsAsStreamV2(filename: String, predicate: MySet => Boolean = _ => true, limit: Int = 10): IO[List[MySet]] =
    Files[IO]
      .readAll(Path(filename))
      .through(text.utf8.decode)
      .through(text.lines)
      .parEvalMapUnbounded( line => IO(parseLine(line))) //parallel parsing
      .unNone
      .filter(predicate)
      .take(limit) // Stream[IO, MySet]
      .compile
      .toList

  def readSetsAsStreamV3(filename: String, predicate: MySet => Boolean = _ => true, limit: Int = 10): IO[List[MySet]] =
    Files[IO]
      .readAll(Path(filename))
      .through(text.utf8.decode)
      .through(text.lines)
      .map(parseLine)
      .metered(1.second)  //throttle the stream, doesn't drop element
      .unNone
      .filter(predicate)
      .take(limit) // Stream[IO, MySet]
      .compile
      .toList

  override def run: IO[Unit] =
    val filename = "sets.csv"
    //IO.println(readSets(filename))
    //readSetsAsStream(filename).map(println)
//    readSetsAsStreamV2(filename).map(println)
    readSetsAsStreamV3(filename).map(println)
