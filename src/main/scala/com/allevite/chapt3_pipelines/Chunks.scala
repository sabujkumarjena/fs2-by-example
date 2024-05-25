package com.allevite.chapt3_pipelines

import cats.effect.{IO, IOApp}
import fs2.*
import fs2.io.file.*

import scala.reflect.ClassTag

object Chunks extends IOApp.Simple:
  val s1 = Stream(1,2,3,4)
  val s2 = Stream(Stream(1,2,3), Stream(4,5,6)).flatten
  val s3 = Stream(1,2) ++ Stream(3) ++ Stream(4,5)
  val s4 = Stream.repeatEval(IO(42)).take(5)
  val s5 = Files[IO].readAll(Path("sets.csv"))
  /**
   *  Characteristics of Chunk
   *    - fast concat
   *    - fast indexing
   *    - avoid copying
   *    - have List like interface
   */
  //fast concat
  val c: Chunk[Int] = Chunk(1,2,3)
  val c2: Chunk[Int] =  Chunk.array(Array(4, 5, 6))
  val c3: Chunk[Int] = Chunk.singleton(10)
  val c4: Chunk[Int] = Chunk.empty

  val c5 = c ++ c2 ++ c3 ++ c4

  val a = new Array[Int](3)
  c.copyToArray(a)

  //Exercise
  def compact[A: ClassTag](chunk: Chunk[A]): Chunk[A] =
    val arr = new Array[A](chunk.size)
    chunk.copyToArray(arr)
    Chunk.array(arr)

  override def run: IO[Unit] =
//    IO.println(s1.chunks.toList) //List(Chunk(1, 2, 3, 4))
//    IO.println(s2.chunks.toList) //List(Chunk(1, 2, 3), Chunk(4, 5, 6))
//    IO.println(s3.chunks.toList) //List(Chunk(1, 2), Chunk(3), Chunk(4, 5))
//    s4.chunks.compile.toList.flatMap(IO.println) //List(Chunk(42), Chunk(42), Chunk(42), Chunk(42), Chunk(42))
//    s5.chunks.map(_.size).compile.toList.flatMap(IO.println) //List(65536, 65536, 65536, 65536, 65536, 65536, 65536, 48762)
//    IO.println(c) //Chunk(1, 2, 3)
    IO.println(c5) //Chunk(1, 2, 3, 4, 5, 6, 10)
    IO.println(c5(4))
    IO.println(c5.size)
    IO.println(c.flatMap(i => Chunk(i, i + 1))) //Chunk(1, 2, 2, 3, 3, 4)
    IO { println(a.toList) }
    IO.println(c5.compact)
    IO.println(compact(c5))