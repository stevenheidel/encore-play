name := "encore"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "com.netaporter" %% "scala-uri" % "0.4.1",
  jdbc,
  anorm,
  cache
)

play.Project.playScalaSettings
