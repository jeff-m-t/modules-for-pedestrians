name := "modules-for-pedestrians"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-stm" %% "scala-stm" % "0.7",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0.3",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.0",
  "org.scalaz.stream" %% "scalaz-stream" % "0.8",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test,it"
)

sources in IntegrationTest <++= scalaSource in Test map { root =>
  Seq(
    root / "pedestrian/modules/kv/KVStoreSupportCommonTests.scala"
  )
}
