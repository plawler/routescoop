name := """routescoop-web"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.12"

// Resolvers
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

// Dependencies
libraryDependencies ++= Seq(
  ws,
  ehcache,
  guice, 
  "io.lemonlabs" %% "scala-uri" % "1.1.2",
  "org.mockito" % "mockito-core" % "2.22.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

// Web Jars Dependencies
libraryDependencies ++= Seq(
  "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4",
  "org.webjars" % "bootstrap" % "4.1.3" exclude("org.webjars", "jquery"),
  "org.webjars" % "bootstrap-datepicker" % "1.4.0" exclude("org.webjars", "bootstrap"),
  "org.webjars" % "chartjs" % "2.7.3",
  "org.webjars" % "jquery" % "3.3.1-1",
  "org.webjars" % "font-awesome" % "4.7.0",
  "org.webjars" % "d3js" % "5.9.7",
  "org.webjars" % "vue" % "2.6.12"
)
