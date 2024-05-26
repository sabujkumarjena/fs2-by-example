package com.allevite.chapt5_Communication
import fs2.*
import cats.effect.{IO, IOApp}
import fs2.concurrent.Topic

import scala.concurrent.duration.*
import scala.util.Random
object Topics extends IOApp.Simple:
  val t1 = Stream.eval(Topic[IO, Int]).flatMap { topic =>
    val p = Stream.iterate(1)(_ +1).covary[IO].evalTap(i => IO.println(f"Writing $i")).metered(200.millis).through(topic.publish).drain //producer
    val c1 = topic.subscribe(10).evalMap(i => IO.println(s"Read $i from c1")).metered(500.millis).drain
    val c2 = topic.subscribe(10).evalMap(i => IO.println(s"Read $i from c2")).drain
    Stream(p, c1, c2).parJoinUnbounded
  }

  case class CarPos(carId: Long, lat: Double, lng: Double)
  def createCar(carId: Long, topic: Topic[IO, CarPos]): Stream[IO, Nothing] =
    Stream
      .repeatEval(IO(CarPos(carId, Random.between(-90, 90), Random.between(-180, 180))))
      .metered(1.second)
      .through(topic.publish)
      .drain
  def createGoogleMapUpdater(topic: Topic[IO, CarPos]): Stream[IO, Nothing] =
    topic
      .subscribe(10)
      .evalMap(pos => IO.println(f"Drawing position (${pos.lat}%.2f, ${pos.lng}%.2f) for car ${pos.carId} in map ... "))
      .drain
  //Exercise
  def createDriverNotifier(
      topic: Topic[IO, CarPos],
      shouldNotify: CarPos => Boolean,
      notify: CarPos => IO[Unit]
                          ): Stream[IO, Nothing] =
    topic
      .subscribe(10)
      .evalMap(pos => if shouldNotify(pos) then notify(pos) else IO.unit)
      .drain

  val t3: Stream[IO, Nothing] = Stream.eval(Topic[IO, CarPos]).flatMap { topic =>
    val cars = Stream.range(1, 10).map(carId => createCar(carId, topic))
    val googlMapUpdater = createGoogleMapUpdater(topic)
    val driverNotifier = createDriverNotifier(
      topic = topic,
      shouldNotify = pos => pos.lat > 0,
      notify = pos => IO.println(f"Car ${pos.carId}: you are above the equator ! (${pos.lat}%.2f), ${pos.lng}%.2f !")
    )
    (cars ++ Stream(googlMapUpdater, driverNotifier)).parJoinUnbounded
  }


  override def run: IO[Unit] =
    t1.interruptAfter(3.seconds).compile.drain
    t3.interruptAfter(5.seconds).compile.drain
