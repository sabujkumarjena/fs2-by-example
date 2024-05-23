package com.allevite

import cats.effect.{IO, IOApp}

object WhiteBoard extends IOApp.Simple :
  override def run: IO[Unit] = IO.println("Hello World")

object MyTest extends App {
  println ("sabuj")
  def repeat(n: Int): Int =
    if( n < 0) 0
    else if (n % 2 == 0) n/2
    else 3*n + 1

  def loop(s: Int, n: Int): Int =
    if(n == 1) s
    else loop(s +1, repeat(n))

  def steps(number: Int): Int = loop(1, number)

  println(steps(10))
  println(steps(1))


}