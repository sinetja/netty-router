organization := "tv.cntt"

name         := "netty-router"

version      := "1.10-SNAPSHOT"

//------------------------------------------------------------------------------

scalaVersion := "2.11.2"

autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

javacOptions in (Compile) ++= Seq("-source", "1.5", "-target", "1.5", "-Xlint:deprecation")

javacOptions in (Compile, doc) := Seq("-source", "1.5")

//------------------------------------------------------------------------------

libraryDependencies += "tv.cntt" % "jauter" % "1.7"

libraryDependencies += "io.netty" % "netty-all" % "4.0.24.Final" % "provided"
