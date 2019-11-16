name := "redis-proxy"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.10",
  "com.typesafe.akka" %% "akka-actor" % "2.5.24",
  "com.typesafe.akka" %% "akka-stream" % "2.5.24",
  "com.typesafe" % "config" % "1.3.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.24" % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "com.typesafe.akka" %% "akka-http-core" % "10.1.9",
  "com.typesafe.akka" %% "akka-http" % "10.1.9",
  "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.4.2",
  "joda-time" % "joda-time" % "2.10.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9"
)
