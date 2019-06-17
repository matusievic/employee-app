name := """employee-app"""

version := "1.0"

scalaVersion := "2.11.8"

val testVersion = "3.0.5"
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % testVersion % Test,
  "org.scalactic" %% "scalactic" % testVersion
)

val akkaVersion = "2.5.0"
val akkaHttpVersion = "10.0.15"
libraryDependencies ++= Seq(
  "com.typesafe.akka"   %% "akka-actor"                % akkaVersion,
  "com.typesafe.akka"   %% "akka-persistence"          % akkaVersion,
  "com.typesafe.akka"   %% "akka-http"                 % akkaHttpVersion,
  "com.typesafe.akka"   %% "akka-http-testkit"         % akkaHttpVersion % Test,
  "com.typesafe.akka"   %% "akka-testkit"              % akkaVersion     % Test
)

libraryDependencies ++= Seq(
  "com.github.scullxbones" %% "akka-persistence-mongo-scala"   % "2.2.7"
)

val mongoDriverVersion = "2.6.0"
libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % mongoDriverVersion,
  "org.mongodb.scala" %% "mongo-scala-bson"   % mongoDriverVersion
)

libraryDependencies ++= Seq(
  "org.slf4j"                % "slf4j-log4j12"    % "1.7.26",
  "org.apache.logging.log4j" % "log4j-core"       % "2.11.2"
)

libraryDependencies ++= Seq(
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.15.1"
)

resolvers += "dnvriend" at "http://dl.bintray.com/dnvriend/maven"
resolvers += Resolver.jcenterRepo
