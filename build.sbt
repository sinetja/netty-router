organization := "tv.cntt"
name         := "netty-router"
version      := "2.2.0-SNAPSHOT"

//------------------------------------------------------------------------------

// This project does not use Scala, only SBT as build tool
autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

// Netty 4+ requires Java 6
javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:deprecation")

javacOptions in (Compile, doc) := Seq("-source", "1.6")

//------------------------------------------------------------------------------

libraryDependencies += "io.netty" % "netty-all" % "4.1.11.Final" % "provided"
libraryDependencies += "junit"    % "junit"     % "4.12"         % "test"
