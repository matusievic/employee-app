package by.epmloyee.app.actor.employee

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.persistence.inmemory.extension.{InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import by.epmloyee.app.actor.employee.EmployeeActor._
import by.epmloyee.app.actor.employee.util.RestartableActor
import by.epmloyee.app.actor.employee.util.RestartableActor.RestartActor
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

class EmployeeActorTest extends TestKit(ActorSystem("ActorSystem")) with WordSpecLike
                                                                    with Matchers
                                                                    with BeforeAndAfterAll
                                                                    with BeforeAndAfterEach
                                                                    with ImplicitSender {
  val actor: ActorRef = system.actorOf(Props(new EmployeeActor() with RestartableActor))


  override protected def beforeEach(): Unit = {
    val tp = TestProbe()
    tp.send(StorageExtension(system).journalStorage, InMemoryJournalStorage.ClearJournal)
    tp.expectMsg(akka.actor.Status.Success(""))
    tp.send(StorageExtension(system).snapshotStorage, InMemorySnapshotStorage.ClearSnapshots)
    tp.expectMsg(akka.actor.Status.Success(""))
    actor ! RestartActor
  }

  "EmployeeActor" should {
    val numberToAdd = "777"
    val indexToUpdate = 0
    val numberToUpdate = "555"
    "add a phone to list and preserve it after restart" in {
      actor ! AddPhoneRequest(numberToAdd)
      expectMsg(AddPhoneResponse(Some(numberToAdd)))

      actor ! RestartActor
      actor ! ReadAllPhonesRequest()
      expectMsg(ReadAllPhonesResponse(Seq(numberToAdd)))
    }
    "update a phone in list and preserve it after restart" in {
      actor ! AddPhoneRequest(numberToAdd)
      expectMsg(AddPhoneResponse(Some(numberToAdd)))
      actor ! UpdatePhoneRequest(indexToUpdate, numberToUpdate)
      expectMsg(UpdatePhoneResponse(Some(numberToUpdate)))

      actor ! RestartActor
      actor ! ReadAllPhonesRequest
      expectMsg(ReadAllPhonesResponse(Seq(numberToUpdate)))
    }
  }


  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
