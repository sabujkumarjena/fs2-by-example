package com.allevite

import cats.effect.{IO, IOApp}

object WhiteBoard extends IOApp.Simple :
  override def run: IO[Unit] = IO.println("Hello World")
