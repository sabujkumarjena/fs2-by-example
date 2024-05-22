ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.1"   //"3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "fs2-by-example",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.10.2",
      // optional I/O library
      "co.fs2" %% "fs2-io" % "3.10.2",
      // optional reactive streams interop
      //"co.fs2" %% "fs2-reactive-streams" % "3.10.2"
      // optional scodec interop
      //"co.fs2" %% "fs2-scodec" % "3.10.2"
    )
  )
