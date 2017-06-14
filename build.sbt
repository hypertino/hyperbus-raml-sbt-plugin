scalaVersion := "2.10.6"

organization := "com.hypertino"

name := "hyperbus-raml-sbt-plugin"

version := "0.2-SNAPSHOT"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.hypertino" %% "binders" % "1.0-SNAPSHOT",
  "com.hypertino" % "raml-parser-2" % "1.0.5-SNAPSHOT",
  "com.hypertino" %% "hyperbus-raml-utils" % "0.1-SNAPSHOT",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "com.hypertino" %% "scalamock-scalatest-support" % "3.4-SNAPSHOT" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.8" % "test",
  "org.bitbucket.cowwoc" % "diff-match-patch" % "1.1" % "test"
//  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("public")
)

fork in Test := true
