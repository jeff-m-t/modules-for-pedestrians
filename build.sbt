name := "modules-for-pedestrians"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-stm" %% "scala-stm" % "0.7",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0.1",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test,it"
)
