package com.allevite.chapt2_effectful
import cats.effect.{IO, IOApp}
import fs2.*

object Create extends IOApp.Simple :

  val s: Stream[IO, Unit] = Stream.eval(IO.println("effectful stream"))
  val s2: Stream[IO, Nothing] = Stream.exec(IO.println("effectful stream 2"))
  val pureStream : Stream[Pure, Int] = Stream(1,2,3,4)
  val s3: Stream[IO, Int] = pureStream.covary[IO]
  val natsEval: Stream[IO, Int] = Stream.iterateEval(1)(a => IO.println(s"Producing ${a + 1}") *> IO(a + 1))
  val alphabet: Stream[IO, Char] = Stream.unfoldEval('a') { c =>
    if c == 'z' + 1 then IO.println("Finishing") >> IO.pure(None) else IO.println(s"Producing $c") >> IO.pure(Some((c, (c + 1).toChar)))
  }

  override def run: IO[Unit] =
    //IO.println("sabuj")
    //s.compile.toList.flatMap(IO.println)
    //s.compile.drain // only execute the effects of the stream
    //s2.compile.drain
    //s3.compile.drain
//    s3.compile.toList.flatMap(IO.println)
   // natsEval.take(5).compile.toList.flatMap(IO.println)
   alphabet.compile.toList.flatMap(IO.println)