
name := "SAFIR"

version := "0.1"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.12",
  "org.scalactic" %% "scalactic" % "3.0.5"
)

mainClass in assembly := Some("Main")

assemblyJarName in assembly := "safir.jar"
