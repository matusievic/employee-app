package by.epmloyee.app.actor.employee

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import by.epmloyee.app.actor.employee.EmployeeActor._

class EmployeeActor extends PersistentActor with ActorLogging {
  var state = State(1L, "First", "First", List(), 1, 5)

  override def receiveRecover: Receive = {
    case PhoneAddedEvent(phone, snapshotTimer) =>
      state = state.addPhone(phone).snapshotTimer(snapshotTimer)
    case PhoneUpdatedEvent(index, phone, snapshotTimer) =>
      state = state.updatePhone(index, phone).snapshotTimer(snapshotTimer)
    case PhoneDeletedEvent(phone, snapshotTimer) =>
      state = state.deletePhone(phone).snapshotTimer(snapshotTimer)
    case SnapshotOffer(_, s: State) =>
      state = s
  }

  override def receiveCommand: Receive = {
    case AddPhoneRequest(phone) =>
      state.phones.find(_ == phone) match {
        case Some(_) =>
          sender ! AddPhoneResponse(None)
        case None =>
          state = state.addPhone(phone)
          persist(PhoneAddedEvent(phone, state.snapshotTimer))
          sender ! AddPhoneResponse(Some(phone))
      }

    case ReadAllPhonesRequest() =>
      sender ! ReadAllPhonesResponse(state.phones)

    case ReadPhoneRequest(index) =>
      sender ! ReadPhoneResponse(state.phones.lift(index))

    case UpdatePhoneRequest(index, phone) =>
      state.phones.lift(index) match {
        case Some(_) =>
          state = state.updatePhone(index, phone)
          persist(PhoneUpdatedEvent(index, phone, state.snapshotTimer))
          sender ! UpdatePhoneResponse(Some(phone))
        case None =>
          sender ! UpdatePhoneResponse(None)
      }

    case DeletePhoneRequest(index) =>
      state.phones.lift(index) match {
        case Some(phone) =>
          state = state.deletePhone(phone)
          persist(PhoneDeletedEvent(phone, state.snapshotTimer))
          sender ! DeletePhoneResponse(Some(phone))
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


  case class State(id: Long, name: String, surname: String, phones: List[Phone], salary: Double, snapshotTimer: Int) {
    def addPhone(phone: Phone): State = copy(phones = phones :+ phone)
    def updatePhone(index: Int, number: Phone): State = copy(phones = phones.updated(index, number))
    def deletePhone(phone: Phone): State = copy(phones = phones.filterNot(_ == phone))
    def decrementSnapshotTimer(): State = copy(snapshotTimer = snapshotTimer - 1)
    def resetSnapshotTimer(): State = copy(snapshotTimer = 5)
    def snapshotTimer(snapshotTimer: Int): State = copy(snapshotTimer = snapshotTimer)
  }

  case class Phone(code: String, number: String)


  sealed trait EmployeeMessage

  case class AddPhoneRequest(phone: Phone) extends EmployeeMessage
  case class AddPhoneResponse(result: Option[Phone]) extends EmployeeMessage

  case class ReadAllPhonesRequest() extends EmployeeMessage
  case class ReadAllPhonesResponse(result: Seq[Phone]) extends EmployeeMessage

  case class ReadPhoneRequest(index: Int) extends EmployeeMessage
  case class ReadPhoneResponse(result: Option[Phone]) extends EmployeeMessage

  case class UpdatePhoneRequest(index: Int, phone: Phone) extends EmployeeMessage
  case class UpdatePhoneResponse(index: Option[Phone]) extends EmployeeMessage

  case class DeletePhoneRequest(index: Int) extends EmployeeMessage
  case class DeletePhoneResponse(result: Option[Phone]) extends EmployeeMessage


  sealed trait EmployeeEvent

  case class PhoneAddedEvent(phone: Phone, snapshotTimer: Int) extends EmployeeEvent
  case class PhoneUpdatedEvent(index: Int, phone: Phone, snapshotTimer: Int) extends EmployeeEvent
  case class PhoneDeletedEvent(phone: Phone, snapshotTimer: Int) extends EmployeeEvent
}