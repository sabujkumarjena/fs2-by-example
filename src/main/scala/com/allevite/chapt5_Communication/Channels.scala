package com.allevite.chapt5_Communication
import cats.Order
import cats.effect.{IO, IOApp}
import fs2.*
import fs2.concurrent.Channel

import scala.concurrent.duration.*
import scala.util.Random
import cats.implicits.*

object Channels extends IOApp.Simple:
  val s: Stream[IO, Nothing] =
    Stream.eval(Channel.unbounded[IO, Int]).flatMap { channel =>
      val p = Stream.iterate(1)(_ + 1).covary[IO].evalMap(channel.send).drain //producer
      val c = channel.stream.evalMap(i => IO.println(s" Read $i")).drain //consumer
      c.concurrently(p)
    }

  val s2: Stream[IO, Nothing] =
    Stream.eval(Channel.bounded[IO, Int](1)).flatMap { channel =>
      val p = Stream.iterate(1)(_ + 1).covary[IO].evalMap(channel.send).drain //producer
      val c = channel.stream.evalMap(i => IO.println(s" Read $i")).drain //consumer
      c.concurrently(p)
    }

  val s3: Stream[IO, Nothing] =
    Stream.eval(Channel.bounded[IO, Int](1)).flatMap { channel =>
      val p = Stream.iterate(1)(_ + 1).covary[IO].evalMap(channel.send).drain //producer
      val c = channel.stream.metered(200.millis).evalMap(i => IO.println(s" Read $i")).drain //consumer
      c.concurrently(p)
    }

  sealed trait Measurement
  case class Temperature(value: Double) extends Measurement
  case class Humidity(value: Double) extends Measurement

  given ordHum:Order[Humidity] =Order.by(_.value)
  given ordTemp:Order[Temperature] =Order.by(_.value)

  def createTemperatureSensor(alarm: Channel[IO, Measurement], threshold: Temperature): Stream[IO, Nothing] =
    Stream
      .repeatEval(IO(Temperature(Random.between(-40.0, 40.0))))
      .evalTap(t => IO.println(f"Current temperature: ${t.value}%.1f"))
      .evalMap(t => if t > threshold then alarm.send(t) else IO.unit)
      .metered(300.millis)
      .drain

  def createHumiditySensor(alarm: Channel[IO, Measurement], threshold: Humidity): Stream[IO, Nothing] =
    Stream
      .repeatEval(IO(Humidity(Random.between(0.0, 100.0))))
      .evalTap(h => IO.println(f"Current humidity: ${h.value}%.1f"))
      .evalMap(h => if h > threshold then alarm.send(h) else IO.unit)
      .metered(100.millis)
      .drain
  //Read the values from the channel
  //Handle alarms by
  def createCooler(alarm: Channel[IO, Measurement]): Stream[IO, Nothing] =
    alarm
      .stream
      .evalMap {
        case Temperature(t) => IO.println(f"$t%.1f °C is too hot! Cooling down... ")
        case Humidity(h) => IO.println(f"$h%.1f %% is too humid! Drying...")
      }
      .drain
  val tempThreshhold = Temperature(10.0)
  val humidityThreshold =  Humidity(50.0)

  //Exercise
  //Create a stream that emits a new unbound channel
  //Create one of each sensor and a cooler
  //Run all streams concurrently
  val program = Stream.eval(Channel.unbounded[IO, Measurement]).flatMap { alarmChannel =>
    val tempSensor = createTemperatureSensor(alarmChannel, tempThreshhold)
    val humiditySensor = createHumiditySensor(alarmChannel, humidityThreshold)
    val cooler = createCooler(alarmChannel)
    Stream(tempSensor, humiditySensor, cooler).parJoinUnbounded
  }

  override def run: IO[Unit] =
    s.interruptAfter(5.seconds).compile.drain
    s2.interruptAfter(5.seconds).compile.drain
    s3.interruptAfter(5.seconds).compile.drain
    program.interruptAfter(3.seconds).compile.drain
