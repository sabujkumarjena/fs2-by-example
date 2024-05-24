package com.allevite.chapt2_effectful

import cats.effect.{IO, IOApp}
import fs2.*

import scala.concurrent.duration.*
import scala.util.Random

object Retry extends IOApp.Simple:
  def doEffectFailing[A](io: IO[A]) =
    IO(math.random()).flatMap { flag =>
      if flag < 0.5 then IO.println("Failing..") >> IO.raiseError(new Exception("boom"))
      else IO.println(" Successfull..") >> io
    }

  //Excercise
  val seraches: Stream[IO, String] = Stream.iterateEval("")(s => IO(Random.nextPrintableChar()).map(s + _))
  def performSearch(text: String): IO[Unit] = doEffectFailing(IO.println(s"Performing search for text: $text"))
  def performSearchRetrying(text: String): Stream[IO, Unit] =
    // the delays should be 1s, 2s, 3s, 4s,..maximum attempt is 5
    //Process the searches
    // 1 - Simulate that the user enters a char every 200 millis
    // 2- Sample the search string every 500 millis
    // 3- Run the processing for 5 secs

    Stream.retry(
      fo = performSearch(text),
      delay = 1.second,
      nextDelay =  _ + 1.second,
      maxAttempts = 5
    )


  override def run: IO[Unit] =
    Stream.eval(doEffectFailing(IO(45)))
      .compile
      .toList
      .flatMap(IO.println)

    Stream.retry(
      fo = doEffectFailing(IO(45)),
      delay = 1.seconds,
      nextDelay = _ * 2,
      maxAttempts =  3
    ).compile.drain

    seraches
      .metered(200.millis)
      .debounce(500.millis)
      .flatMap(performSearchRetrying)
      .interruptAfter(5.seconds)
      .compile
      .drain
