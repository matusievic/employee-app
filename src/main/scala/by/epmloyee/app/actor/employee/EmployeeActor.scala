package by.epmloyee.app.actor.employee

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import by.epmloyee.app.actor.common.Result
import by.epmloyee.app.actor.common.error.{InvalidParam, NotFound}
import by.epmloyee.app.actor.employee.EmployeeActor._

class EmployeeActor extends PersistentActor with ActorLogging {
  var state = State(1L, "First", "First", List.empty[Phone], 1)

  override def receiveRecover: Receive = {
    case PhoneAddedEvent(phone) =>
      state = state.addPhone(phone)
    case PhoneUpdatedEvent(index, phone) =>
      state = state.updatePhone(index, phone)
    case PhoneDeletedEvent(phone) =>
      state = state.deletePhone(phone)
    case SnapshotOffer(_, s: State) =>
      state = s
  }

  override def receiveCommand: Receive = {
    case AddPhone(phone) =>
      state.phones.find(_ == phone) match {
        case Some(_) =>
          sender ! Left(InvalidParam("This phone already present"))
        case None =>
          state = state.addPhone(phone)
          persist(PhoneAddedEvent(phone))
          sender ! Right(phone)
      }

    case ReadAllPhones() =>
      sender ! Right(state.phones)

    case ReadPhone(index) =>
      sender ! state.phones.lift(index).fold(Left(InvalidParam("There's no phone with such index")) : Result[Phone])(p => Right(p))

    case UpdatePhone(index, phone) =>
      state.phones.lift(index) match {
        case Some(_) =>
          state = state.updatePhone(index, phone)
          persist(PhoneUpdatedEvent(index, phone))
          sender ! Right(phone)
        case None =>
          sender ! Left(InvalidParam("There's no phone with such index"))
      }

    case DeletePhone(index) =>
      state.phones.lift(index) match {
        case Some(phone) =>
          state = state.deletePhone(phone)
          persist(PhoneDeletedEvent(phone))
          sender ! Right(phone)
        case None =>
          sender ! Left(NotFound("There's no phone with such index"))
      }
  }

  override def persistenceId: String = self.path.name

  def persist(event: EmployeeEvent): Unit = {
    super.persist(event) { _ =>
      context.system.eventStream.publish(event)
    }
    if (lastSequenceNr % 5 == 0 && lastSequenceNr != 0) {
      saveSnapshot(state)
    }
  }
}

object EmployeeActor {
  val props = Props(classOf[EmployeeActor])


  case class State(id: Long, name: String, surname: String, phones: List[Phone], salary: Double) {
    def addPhone(phone: Phone): State = copy(phones = phones :+ phone)
    def updatePhone(index: Int, number: Phone): State = copy(phones = phones.updated(index, number))
    def deletePhone(phone: Phone): State = copy(phones = phones.filterNot(_ == phone))
  }

  case class Phone(code: String, number: String)


  sealed trait EmployeeMessage

  case class AddPhone(phone: Phone) extends EmployeeMessage

  case class ReadAllPhones() extends EmployeeMessage

  case class ReadPhone(index: Int) extends EmployeeMessage

  case class UpdatePhone(index: Int, phone: Phone) extends EmployeeMessage

  case class DeletePhone(index: Int) extends EmployeeMessage


  sealed trait EmployeeEvent

  case class PhoneAddedEvent(phone: Phone) extends EmployeeEvent
  case class PhoneUpdatedEvent(index: Int, phone: Phone) extends EmployeeEvent
  case class PhoneDeletedEvent(phone: Phone) extends EmployeeEvent
}