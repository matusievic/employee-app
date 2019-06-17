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
      withActor { actor =>
        val numberToAdd = "777"
        actor ! AddPhoneRequest(numberToAdd)
        expectMsg(AddPhoneResponse(Some(numberToAdd)))

        actor ! RestartActor
        actor ! ReadAllPhonesRequest()
        expectMsg(ReadAllPhonesResponse(Seq("1", numberToAdd)))
      }
    }

    "read a phone to list and preserve it after restart" in {
      withActor { actor =>
        val indexToRead = 0
        val expectedValue = "1"
        actor ! ReadPhoneRequest(indexToRead)
        expectMsg(ReadPhoneResponse(Some(expectedValue)))

        actor ! RestartActor
        actor ! ReadAllPhonesRequest()
        expectMsg(ReadAllPhonesResponse(Seq(expectedValue)))
      }
    }

    "update a phone in list and preserve it after restart" in {
      withActor { actor =>
        val indexToUpdate = 0
        val targetValue = "5"

        actor ! UpdatePhoneRequest(indexToUpdate, targetValue)
        expectMsg(UpdatePhoneResponse(Some(targetValue)))

        actor ! RestartActor
        actor ! ReadAllPhonesRequest()
        expectMsg(ReadAllPhonesResponse(Seq(targetValue)))
      }
    }

    "delete a phone in list and preserve it after restart" in {
      withActor { actor =>
        val indexToDelete = 0
        val numberToDelete = "1"

        actor ! DeletePhoneRequest(indexToDelete)
        expectMsg(DeletePhoneResponse(Some(numberToDelete)))

        actor ! RestartActor
        actor ! ReadAllPhonesRequest()
        expectMsg(ReadAllPhonesResponse(Seq()))
      }
    }
  }

  override protected def afterAll(): Unit = {
    shutdown(system)
  }

  private def withActor(test: ActorRef => Unit): Unit = {
    val actor = makeEmployeeActor()
    test(actor)
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
