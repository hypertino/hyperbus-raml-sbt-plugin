scalaVersion := "2.10.6"

organization := "com.hypertino"

name := "hyperbus-raml-sbt-plugin"

version := "0.4.1-SNAPSHOT"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.hypertino" %% "binders" % "1.2.0",
  "com.hypertino" % "raml-parser-2" % "1.0.16",
  "com.hypertino" %% "hyperbus-utils" % "0.1-SNAPSHOT",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.scalamock"   %% "scalamock-scalatest-support" % "3.5.0" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
  "org.bitbucket.cowwoc" % "diff-match-patch" % "1.1" % "test"
//  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("public")
)

fork in Test := true
