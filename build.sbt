name := """employee-app"""

version := "1.0"

scalaVersion := "2.11.8"

val testVersion = "3.0.5"
val scalaTest = Seq(
  "org.scalatest" %% "scalatest" % testVersion % Test,
  "org.scalactic" %% "scalactic" % testVersion
)

val akkaVersion = "2.5.0"
val akka = Seq(
  "com.typesafe.akka" %% "akka-actor"       % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit"     % akkaVersion  % Test
)

val akkaHttpVersion = "10.0.15"
val akkaHttp = Seq(
  "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)

val mongoDriverVersion = "2.6.0"
val mongo =  Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % mongoDriverVersion,
  "org.mongodb.scala" %% "mongo-scala-bson"   % mongoDriverVersion
)

val log = Seq(
  "org.slf4j"                % "slf4j-log4j12"    % "1.7.26",
  "org.apache.logging.log4j" % "log4j-core"       % "2.11.2"
)

val persistence = Seq(
  "com.github.scullxbones" %% "akka-persistence-mongo-scala"   % "2.2.7",
  "com.github.dnvriend"    %% "akka-persistence-inmemory"      % "2.5.15.1"
)

val circeVersion = "0.11.1"
val circe = Seq(
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion
)

libraryDependencies ++= Seq(
  scalaTest,
  akka,
  akkaHttp,
  mongo,
  log,
  persistence,
  circe
).flatten

resolvers += "dnvriend" at "http://dl.bintray.com/dnvriend/maven"
resolvers += Resolver.jcenterRepo
