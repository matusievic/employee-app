package by.epmloyee.app.route.phone

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import by.epmloyee.app.actor.employee.EmployeeActor._
import by.epmloyee.app.route.directive.AppDirective._
import by.epmloyee.app.route.json.JsonImplicits

import scala.concurrent.ExecutionContext

class PhoneRoutes(val actor: ActorRef)(implicit val executor: ExecutionContext, val timeout: Timeout) extends JsonImplicits {
  val routes: Route = pathPrefix("employee") {
    pathPrefix("phones") {
      pathEndOrSingleSlash {
        get {
          handleRequest[List[Phone]](() => actor ? ReadAllPhones())
        } ~
          post {
            handleRequest[Phone, Phone](phone => actor ? AddPhone(phone))
          }
      } ~
        path(IntNumber) { id =>
          pathEndOrSingleSlash {
            get {
              handleRequest[Phone](() => actor ? ReadPhone(id))
            } ~
              put {
                handleRequest[Phone, Phone](phone => actor ? UpdatePhone(id, phone))
              } ~
              delete {
                handleRequest[Phone](() => actor ? DeletePhone(id))
              }
          }
        }
    }
  }
}

object PhoneRoutes {
  def apply(actor: ActorRef)(implicit executor: ExecutionContext, timeout: Timeout) = new PhoneRoutes(actor)
}