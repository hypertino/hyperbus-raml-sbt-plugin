scalaVersion := "2.12.4"

organization := "com.hypertino"

name := "hyperbus-raml-sbt-plugin"

version := "0.5-SNAPSHOT"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.hypertino" %% "binders" % "1.2.3",
  "com.hypertino" % "raml-parser-2" % "1.0.18",
  "com.hypertino" %% "hyperbus-utils" % "0.2.0",
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
