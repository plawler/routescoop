name := """routescoop-api"""

version := "1.0-SNAPSHOT"

lazy val scrava = RootProject(uri("git://github.com/kiambogo/scrava.git"))
lazy val root = (project in file(".")).dependsOn(scrava).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  cache,
  ws,
  "mysql" % "mysql-connector-java" % "6.0.5",
  "com.typesafe.play" %% "anorm" % "2.5.2",
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

// https://stackoverflow.com/questions/28351405/restarting-play-application-docker-container-results-in-this-application-is-alr

