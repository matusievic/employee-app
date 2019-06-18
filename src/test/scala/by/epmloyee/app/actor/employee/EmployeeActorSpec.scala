package by.epmloyee.app.actor.employee

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import by.epmloyee.app.actor.employee.EmployeeActor._
import by.epmloyee.app.actor.employee.util.RestartableActor
import by.epmloyee.app.actor.employee.util.RestartableActor.RestartActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.Random

class EmployeeActorSpec extends TestKit(ActorSystem("ActorSystem")) with WordSpecLike
                                                                    with Matchers
                                                                    with BeforeAndAfterAll
                                                                    with ImplicitSender {
  "EmployeeActor" should {
    "add a phone to list and preserve it after restart" in {
      { actor: ActorRef =>
        actor ! AddPhoneRequest("777")
        expectMsg(AddPhoneResponse(Some("777")))
      } afterRestartShouldContain ("1", "777")
    }

    "read a phone to list and preserve it after restart" in {
      { actor: ActorRef =>
        actor ! ReadPhoneRequest(0)
        expectMsg(ReadPhoneResponse(Some("1")))
      } afterRestartShouldContain "1"
    }

    "update a phone in list and preserve it after restart" in {
      { actor: ActorRef =>
        actor ! UpdatePhoneRequest(0, "5")
        expectMsg(UpdatePhoneResponse(Some("5")))
      } afterRestartShouldContain "5"
    }

    "delete a phone in list and preserve it after restart" in {
      { actor: ActorRef =>
        actor ! DeletePhoneRequest(0)
        expectMsg(DeletePhoneResponse(Some("1")))
      } afterRestartShouldContain ()
    }
  }

  override protected def afterAll(): Unit = {
    shutdown(system)
  }

  implicit class TestCase(before: ActorRef => Any) extends AnyRef {
    def afterRestartShouldContain(values: String*) = {
      val actor = makeEmployeeActor()
      before(actor)
      actor ! RestartActor
      actor ! ReadAllPhonesRequest()
      expectMsg(ReadAllPhonesResponse(values))
      killActor(actor)
    }

    private def makeEmployeeActor() = {
      val actor = system.actorOf(Props(new EmployeeActor() with RestartableActor), Random.nextInt(100).toString)
      actor ! AddPhoneRequest("1")
      expectMsg(AddPhoneResponse(Some("1")))
      actor
    }

    private def killActor(actor: ActorRef): Unit = {
      actor ! PoisonPill
    }
  }
}
