name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += cache
libraryDependencies += ws
libraryDependencies += evolutions
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
libraryDependencies += "org.virtuslab"          %% "unicorn-play"       % "1.1.0"
libraryDependencies += "org.typelevel"          %% "cats"               % "0.9.0"
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2"
)

// https://mvnrepository.com/artifact/com.h2database/h2
libraryDependencies += "com.h2database" % "h2" % "1.4.193"

// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212"

// https://mvnrepository.com/artifact/org.scalatest/scalatest_2.11
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % Test

// https://mvnrepository.com/artifact/org.scalamock/scalamock-scalatest-support_2.11
libraryDependencies += "org.scalamock" % "scalamock-scalatest-support_2.11" % "3.2.2" % Test
