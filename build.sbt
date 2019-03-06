name := "fired-ammo-extractor"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

mainClass in assembly := Some("im.mak.extractor.Main")
assemblyJarName in assembly := "fired-ammo-extractor.jar"
