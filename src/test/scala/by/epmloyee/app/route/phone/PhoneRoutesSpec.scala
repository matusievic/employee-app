package by.epmloyee.app.route.phone

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import by.epmloyee.app.actor.employee.EmployeeActor.Phone
import by.epmloyee.app.route.json.JsonImplicits
import by.epmloyee.app.route.phone.mock.EmployeeActorMock
import io.circe.Encoder
import io.circe.syntax._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

class PhoneRoutesSpec extends WordSpec
                      with Matchers
                      with ScalatestRouteTest
                      with JsonImplicits {
  val routes: Route = PhoneRoutes(system.actorOf(EmployeeActorMock.props))(executor, 5 seconds).routes
  val sourcePhone = Phone("1", "1")
  val testPhone = Phone("2", "2")
  val sourcePhoneJson: String = sourcePhone.asJson.noSpaces
  val testPhoneJson: String = testPhone.asJson.noSpaces

  "The app" should {
    "return a sequence of phone number if GET without ID" in {
      Get("/employee/phones") ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeSuccessResponse[List[Phone]](List(sourcePhone))
      }
    }

    "return a phone number if GET with a valid ID" in {
      Get("/employee/phones/0") ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeSuccessResponse[Phone](sourcePhone)
      }
    }

    "return none if GET with an invalid ID" in {
      Get("/employee/phones/2") ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeErrorResponse(StatusCodes.NotFound, "Invalid index")
      }
    }


    "return number if POST with a new phone number" in {
      Post("/employee/phones/", testPhoneJson) ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeSuccessResponse[Phone](testPhone)
      }
    }

    "return none if POST with existing phone number" in {
      Post("/employee/phones", sourcePhoneJson) ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeErrorResponse(StatusCodes.BadRequest, "This phone already presented")
      }
    }


    "return number if PUT with a valid phone index" in {
      Put("/employee/phones/0", sourcePhoneJson) ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeSuccessResponse[Phone](sourcePhone)
      }
    }

    "return none if PUT with an invalid phone index" in {
      Put("/employee/phones/3", sourcePhoneJson) ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeErrorResponse(StatusCodes.BadRequest, "Invalid index")
      }
    }


    "return number if DELETE with a valid phone index" in {
      Delete("/employee/phones/0", sourcePhoneJson) ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeSuccessResponse[Phone](sourcePhone)
      }
    }

    "return none if DELETE with an invalid phone index" in {
      Delete("/employee/phones/3", sourcePhoneJson) ~> routes ~> check {
        responseAs[HttpResponse] shouldEqual makeErrorResponse(StatusCodes.NotFound, "Invalid index")
      }
    }
  }

  override protected def afterAll(): Unit = {
    system.terminate()
  }

  private def makeSuccessResponse[T: Encoder](body: T): HttpResponse = {
    HttpResponse(entity = HttpEntity(body.asJson.noSpaces).withContentType(ContentTypes.`application/json`))
  }

  private def makeErrorResponse(code: StatusCode, message: String): HttpResponse = {
    HttpResponse(code, entity = HttpEntity(message.asJson.noSpaces).withContentType(ContentTypes.`application/json`))
  }
}
