organization := "tv.cntt"
name         := "netty-router"
version      := "2.0.0-SNAPSHOT"

//------------------------------------------------------------------------------

// This project does not use Scala
autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

// Netty 4+ requires Java 6
javacOptions in (Compile) ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:deprecation")

javacOptions in (Compile, doc) := Seq("-source", "1.6")

//------------------------------------------------------------------------------

libraryDependencies += "io.netty" % "netty-all" % "4.0.31.Final" % "provided"
libraryDependencies += "junit"    % "junit"     % "4.2"          % "test"
