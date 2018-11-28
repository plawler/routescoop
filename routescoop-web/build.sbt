name := """routescoop-web"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

// Resolvers
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"


// Dependencies
libraryDependencies ++= Seq(
  ws,
  cache,
  "io.lemonlabs" %% "scala-uri" % "1.1.2",
  "org.mockito" % "mockito-core" % "2.22.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

// Web Jars Dependencies
libraryDependencies ++= Seq(
  "com.adrianhurt" %% "play-bootstrap" % "1.4-P25-B4-SNAPSHOT",
  "org.webjars" % "bootstrap" % "4.1.3" exclude("org.webjars", "jquery"),
  "org.webjars" % "bootstrap-datepicker" % "1.4.0" exclude("org.webjars", "bootstrap"),
  "org.webjars" % "jquery" % "3.3.1-1",
  "org.webjars" % "font-awesome" % "4.7.0"
)