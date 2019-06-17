package by.epmloyee.app.actor.employee

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import by.epmloyee.app.actor.employee.EmployeeActor._

class EmployeeActor extends PersistentActor with ActorLogging {
  var state = State(1L, "First", "First", List(), 1, 5)

  override def receiveRecover: Receive = {
    case PhoneAddedEvent(number, snapshotTimer) =>
      state = state.addPhone(number).snapshotTimer(snapshotTimer)
    case PhoneUpdatedEvent(index, number, snapshotTimer) =>
      state = state.updatePhone(index, number).snapshotTimer(snapshotTimer)
    case PhoneDeletedEvent(number, snapshotTimer) =>
      state = state.deletePhone(number).snapshotTimer(snapshotTimer)
    case SnapshotOffer(_, s: State) =>
      state = s
  }

  override def receiveCommand: Receive = {
    case AddPhoneRequest(number) =>
      state.phones.find(_ == number) match {
        case Some(_) =>
          sender ! AddPhoneResponse(None)
        case None =>
          state = state.addPhone(number)
          persist(PhoneAddedEvent(number, state.snapshotTimer))
          sender ! AddPhoneResponse(Some(number))
      }

    case ReadAllPhonesRequest() =>
      sender ! ReadAllPhonesResponse(state.phones)

    case ReadPhoneRequest(index) =>
      sender ! ReadPhoneResponse(state.phones.lift(index))

    case UpdatePhoneRequest(index, number) =>
      state.phones.lift(index) match {
        case Some(_) =>
          state = state.updatePhone(index, number)
          persist(PhoneUpdatedEvent(index, number, state.snapshotTimer))
          sender ! UpdatePhoneResponse(Some(number))
        case None =>
          sender ! UpdatePhoneResponse(None)
      }

    case DeletePhoneRequest(index) =>
      state.phones.lift(index) match {
        case Some(number) =>
          state = state.deletePhone(number)
          persist(PhoneDeletedEvent(number, state.snapshotTimer))
          sender ! DeletePhoneResponse(Some(number))
        case None =>
          sender ! DeletePhoneResponse(None)
      }
  }

  override def persistenceId: String = self.path.name

  def persist(event: EmployeeEvent): Unit = {
    super.persist(event) { e =>
      context.system.eventStream.publish(event)
    }
    state = state.decrementSnapshotTimer()
    if (state.snapshotTimer == 0) {
      saveSnapshot(state)
      state = state.resetSnapshotTimer()
    }
  }
}

object EmployeeActor {
  val props = Props(classOf[EmployeeActor])


  case class State(id: Long, name: String, surname: String, phones: List[String], salary: Double, snapshotTimer: Int) {
    def addPhone(number: String): State = copy(phones = phones :+ number)
    def updatePhone(index: Int, number: String): State = copy(phones = phones.updated(index, number))
    def deletePhone(number: String): State = copy(phones = phones.filterNot(_ == number))
    def decrementSnapshotTimer(): State = copy(snapshotTimer = snapshotTimer - 1)
    def resetSnapshotTimer(): State = copy(snapshotTimer = 5)
    def snapshotTimer(snapshotTimer: Int): State = copy(snapshotTimer = snapshotTimer)
  }


  sealed trait EmployeeMessage

  case class AddPhoneRequest(number: String) extends EmployeeMessage
  case class AddPhoneResponse(result: Option[String]) extends EmployeeMessage

  case class ReadAllPhonesRequest() extends EmployeeMessage
  case class ReadAllPhonesResponse(result: Seq[String]) extends EmployeeMessage

  case class ReadPhoneRequest(index: Int) extends EmployeeMessage
  case class ReadPhoneResponse(result: Option[String]) extends EmployeeMessage

  case class UpdatePhoneRequest(index: Int, number: String) extends EmployeeMessage
  case class UpdatePhoneResponse(index: Option[String]) extends EmployeeMessage

  case class DeletePhoneRequest(index: Int) extends EmployeeMessage
  case class DeletePhoneResponse(result: Option[String]) extends EmployeeMessage


  sealed trait EmployeeEvent

  case class PhoneAddedEvent(number: String, snapshotTimer: Int) extends EmployeeEvent
  case class PhoneUpdatedEvent(index: Int, number: String, snapshotTimer: Int) extends EmployeeEvent
  case class PhoneDeletedEvent(number: String, snapshotTimer: Int) extends EmployeeEvent
}