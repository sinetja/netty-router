organization := "tv.cntt"

name         := "netty-router"

version      := "1.10-SNAPSHOT"

//------------------------------------------------------------------------------

scalaVersion := "2.11.2"

autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

// Netty 4+ requires Java 6
javacOptions in (Compile) ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:deprecation")

javacOptions in (Compile, doc) := Seq("-source", "1.6")

//------------------------------------------------------------------------------

libraryDependencies += "tv.cntt" % "jauter" % "1.7"

libraryDependencies += "io.netty" % "netty-all" % "4.0.24.Final" % "provided"
