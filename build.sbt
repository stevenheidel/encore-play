name := """encore"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT",
  "com.netaporter" %% "scala-uri" % "0.4.2",
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  jdbc,
  anorm,
  cache,
  ws
)
