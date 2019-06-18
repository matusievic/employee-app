package by.epmloyee.app.route.phone

import akka.http.scaladsl.testkit.ScalatestRouteTest
import by.epmloyee.app.actor.employee.EmployeeActor._
import by.epmloyee.app.route.phone.mock.EmployeeActorMock
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

class PhoneRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val routes = PhoneRoutes(system.actorOf(EmployeeActorMock.props))(executor, 5 seconds).routes
  "The app" should {
    "return a sequence of phone number if GET without ID" in {
      Get("/employee/phones") ~> routes ~> check {
        responseAs[String] shouldEqual ReadAllPhonesResponse(Seq("1")).toString
      }
    }

    "return a phone number if GET with a valid ID" in {
      Get("/employee/phones/0") ~> routes ~> check {
        responseAs[String] shouldEqual ReadPhoneResponse(Some("1")).toString
      }
    }

    "return none if GET with an invalid ID" in {
      Get("/employee/phones/2") ~> routes ~> check {
        responseAs[String] shouldEqual ReadPhoneResponse(None).toString
      }
    }


    "return number if POST with a new phone number" in {
      Post("/employee/phones/", Some("2")) ~> routes ~> check {
        responseAs[String] shouldEqual AddPhoneResponse(Some("2")).toString
      }
    }

    "return none if POST with existing phone number" in {
      Post("/employee/phones", Some("1")) ~> routes ~> check {
        responseAs[String] shouldEqual AddPhoneResponse(None).toString
      }
    }


    "return number if PUT with a valid phone index" in {
      Put("/employee/phones/0", Some("1")) ~> routes ~> check {
        responseAs[String] shouldEqual UpdatePhoneResponse(Some("1")).toString
      }
    }

    "return none if PUT with an invalid phone index" in {
      Put("/employee/phones/3", Some("1")) ~> routes ~> check {
        responseAs[String] shouldEqual UpdatePhoneResponse(None).toString
      }
    }


    "return number if DELETE with a valid phone index" in {
      Delete("/employee/phones/0", Some("1")) ~> routes ~> check {
        responseAs[String] shouldEqual DeletePhoneResponse(Some("1")).toString
      }
    }

    "return none if DELETE with an invalid phone index" in {
      Delete("/employee/phones/3", Some("1")) ~> routes ~> check {
        responseAs[String] shouldEqual DeletePhoneResponse(None).toString
      }
    }
  }

  override protected def afterAll(): Unit = {
    system.terminate()
  }
}
