package by.epmloyee.app.actor.employee

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import by.epmloyee.app.actor.employee.EmployeeActor._
import by.epmloyee.app.actor.employee.util.RestartableActor
import by.epmloyee.app.actor.employee.util.RestartableActor.RestartActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class EmployeeActorTest extends TestKit(ActorSystem("ActorSystem")) with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ImplicitSender {

  "EmployeeActor" should {
    val numberToAdd = "777"
    val indexToUpdate = 0
    val numberToUpdate = "555"

    test("add a phone to list and preserve it after restart") { actor =>
      actor ! AddPhoneRequest(numberToAdd)
      expectMsg(AddPhoneResponse(Some(numberToAdd)))

      actor ! RestartActor
      actor ! ReadAllPhonesRequest()
      expectMsg(ReadAllPhonesResponse(Seq(numberToAdd)))
    }

    //"add a phone to list and preserve it after restart" in {
    //  actor ! AddPhoneRequest(numberToAdd)
    //  expectMsg(AddPhoneResponse(Some(numberToAdd)))
    //
    //  actor ! RestartActor
    //  actor ! ReadAllPhonesRequest()
    //  expectMsg(ReadAllPhonesResponse(Seq(numberToAdd)))
    //}
    //
    //"update a phone in list and preserve it after restart" in {
    //  actor ! AddPhoneRequest(numberToAdd)
    //  expectMsg(AddPhoneResponse(Some(numberToAdd)))
    //  actor ! UpdatePhoneRequest(indexToUpdate, numberToUpdate)
    //  expectMsg(UpdatePhoneResponse(Some(numberToUpdate)))
    //
    //  actor ! RestartActor
    //  actor ! ReadAllPhonesRequest
    //  expectMsg(ReadAllPhonesResponse(Seq(numberToUpdate)))
    //}
  }

  private def test(message: String)(test: ActorRef => Any): Unit = {
    val actor = makeEmployeeActor()
    message in test(actor)
    killActor(actor)
  }

  private def makeEmployeeActor() = {
    val actor = system.actorOf(Props(new EmployeeActor() with RestartableActor))
    actor ! AddPhoneRequest("1")
    expectMsg(AddPhoneResponse(Some("1")))
    actor
  }

  private def killActor(actor: ActorRef): Unit = {
    actor ! PoisonPill
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
