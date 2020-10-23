name := """routescoop-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.12"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  ws,
  guice,
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "com.typesafe.play" %% "play-json" % "2.6.8",
  "org.playframework.anorm" %% "anorm" % "2.6.0",
  "org.mockito" % "mockito-core" % "3.5.15" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.4.16" % Test
)

// https://stackoverflow.com/questions/28351405/restarting-play-application-docker-container-results-in-this-application-is-alr

