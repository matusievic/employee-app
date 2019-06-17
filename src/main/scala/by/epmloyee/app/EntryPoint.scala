package by.epmloyee.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import by.epmloyee.app.actor.employee.EmployeeActor
import by.epmloyee.app.route.phone.PhoneRoutes
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

object EntryPoint extends App {
  implicit val httpConf: Config = ConfigFactory.load("http.conf")
  implicit val system: ActorSystem = ActorSystem("ActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = 5 seconds

  val employeeActor = system.actorOf(EmployeeActor.props)

  val host = httpConf.getString("http.server.host")
  val port = httpConf.getInt("http.server.port")
  val bindingFuture = Http().bindAndHandle(PhoneRoutes(employeeActor).routes, host, port)
  println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
