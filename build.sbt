organization := "tv.cntt"

name         := "netty-router"

version      := "1.1-SNAPSHOT"

//------------------------------------------------------------------------------

scalaVersion := "2.11.2"

autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

javacOptions in (Compile) ++= Seq("-source", "1.5", "-target", "1.5", "-Xlint:deprecation")

javacOptions in (Compile, doc) := Seq("-source", "1.5")

//------------------------------------------------------------------------------

libraryDependencies += "tv.cntt" % "jauter" % "1.2"

libraryDependencies += "io.netty" % "netty-all" % "4.0.23.Final" % "provided"
