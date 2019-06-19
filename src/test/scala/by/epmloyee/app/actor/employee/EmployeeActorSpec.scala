package by.epmloyee.app.actor.employee

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import by.epmloyee.app.actor.employee.EmployeeActor._
import by.epmloyee.app.actor.employee.util.RestartableActor
import by.epmloyee.app.actor.employee.util.RestartableActor.RestartActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.Random

class EmployeeActorSpec extends TestKit(ActorSystem("ActorSystem"))
                        with WordSpecLike
                        with Matchers
                        with BeforeAndAfterAll
                        with ImplicitSender {
  val sourcePhone = Phone("1", "1")
  val testPhone = Phone("2", "2")

  "EmployeeActor" should {
    "add a phone to list and preserve it after restart" in {
      { actor: ActorRef =>
        actor ! AddPhone(testPhone)
        expectMsg(Right(testPhone))
      } afterRestartShouldContain (sourcePhone, testPhone)
    }

    "read a phone to list and preserve it after restart" in {
      { actor: ActorRef =>
        actor ! ReadPhone(0)
        expectMsg(Right(sourcePhone))
      } afterRestartShouldContain sourcePhone
    }

    "update a phone in list and preserve it after restart" in {
      { actor: ActorRef =>
        actor ! UpdatePhone(0, testPhone)
        expectMsg(Right(testPhone))
      } afterRestartShouldContain testPhone
    }

    "delete a phone in list and preserve it after restart" in {
      { actor: ActorRef =>
        actor ! DeletePhone(0)
        expectMsg(Right(sourcePhone))
      } afterRestartShouldContain ()
    }
  }

  override protected def afterAll(): Unit = {
    shutdown(system)
  }

  implicit class TestCase(before: ActorRef => Any) extends AnyRef {
    def afterRestartShouldContain(values: Phone*): Unit = {
      val actor = makeEmployeeActor()
      before(actor)
      actor ! RestartActor
      actor ! ReadAllPhones()
      expectMsg(Right(values))
      killActor(actor)
    }

    private def makeEmployeeActor(): ActorRef = {
      val actor = system.actorOf(Props(new EmployeeActor() with RestartableActor), Random.nextInt(100).toString)
      actor ! AddPhone(sourcePhone)
      expectMsg(Right(sourcePhone))
      actor
    }

    private def killActor(actor: ActorRef): Unit = {
      actor ! PoisonPill
    }
  }

}
