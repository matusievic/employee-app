package by.epmloyee.app.route.phone

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import by.epmloyee.app.actor.employee.EmployeeActor._

import scala.concurrent.ExecutionContext

class PhoneRoutes(val actor: ActorRef)(implicit val executor: ExecutionContext, val timeout: Timeout) {
  val routes: Route = pathPrefix("employee") {
    pathPrefix("phones") {
      pathEnd {
        get {
          complete((actor ? ReadAllPhonesRequest()).map(resp => HttpResponse(entity = resp.toString)))
        } ~
          post {
            entity(as[String]) { body =>
              complete((actor ? AddPhoneRequest(body)).map(resp => HttpResponse(entity = resp.toString)))
            }
          }
      } ~
        path(IntNumber) { id =>
          get {
            complete((actor ? ReadPhoneRequest(id)).map(resp => HttpResponse(entity = resp.toString)))
          } ~
            put {
              entity(as[String]) { body =>
                complete((actor ? UpdatePhoneRequest(id, body)).map(resp => HttpResponse(entity = resp.toString)))
              }
            } ~
            delete {
              complete((actor ? DeletePhoneRequest(id)).map(resp => HttpResponse(entity = resp.toString)))
            }
        }
    }
  }
}

object PhoneRoutes {
  def apply(actor: ActorRef)(implicit executor: ExecutionContext, timeout: Timeout) = new PhoneRoutes(actor)
}