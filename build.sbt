name := """encore"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT",
  "com.netaporter" %% "scala-uri" % "0.4.2",
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  //"com.wordnik" %% "swagger-play2" % "1.3.6",
  "net.liftweb" % "lift-json_2.11" % "2.6-M4",
  jdbc,
  anorm,
  cache,
  ws
)

scalacOptions ++= Seq("-feature")