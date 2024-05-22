package com.allevite

import cats.effect.{IO, IOApp}

import scala.io.Source
import scala.util.{Try, Using}

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


  override def run: IO[Unit] =
    val filename = "sets.csv"
    IO.println(readSets(filename))
