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
  // Apache Commons
  "commons-io" % "commons-io" % "2.4",
  "commons-codec" % "commons-codec" % "1.9",
  // New Relic
  "com.newrelic.agent.java" % "newrelic-agent" % "3.8.0",
  "com.newrelic.agent.java" % "newrelic-api" % "3.8.0",
  // Defaults
  //jdbc,
  //anorm,
  cache,
  ws
)

scalacOptions ++= Seq("-feature")