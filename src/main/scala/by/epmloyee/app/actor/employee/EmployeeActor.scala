package by.epmloyee.app.actor.employee

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import by.epmloyee.app.actor.employee.EmployeeActor._

class EmployeeActor extends PersistentActor with ActorLogging {
  var state = State(1L, "First", "First", List("111"), 1)
  var snapshotTimer = 5

  override def receiveRecover: Receive = {
    case PhoneAddedEvent(number) =>
      state = state.addPhone(number)
    case PhoneUpdatedEvent(index, number) =>
      state = state.updatePhone(index, number)
    case PhoneDeletedEvent(number) =>
      state = state.deletePhone(number)
  }

  override def receiveCommand: Receive = {
    case AddPhoneRequest(number) =>
      state.phones.find(_ == number) match {
        case Some(_) =>
          sender ! AddPhoneResponse(None)
        case None =>
          state = state.addPhone(number)
          persist(PhoneAddedEvent(number))
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
          persist(PhoneUpdatedEvent(index, number))
          sender ! AddPhoneResponse(Some(number))
        case None =>
          sender ! AddPhoneResponse(None)
      }

    case DeletePhoneRequest(index) =>
      state.phones.lift(index) match {
        case Some(number) =>
          state = state.deletePhone(number)
          persist(PhoneDeletedEvent(number))
        case None =>
          sender ! DeletePhoneResponse(None)
      }
  }

  override def persistenceId: String = self.path.name

  def persist(event: EmployeeEvent): Unit = {
    super.persist(event) { e =>
      context.system.eventStream.publish(event)
    }
    snapshotTimer -= 1
    if (snapshotTimer == 0) {
      saveSnapshot(state)
      snapshotTimer = 5
    }
  }
}

object EmployeeActor {
  val props = Props(classOf[EmployeeActor])


  case class State(id: Long, name: String, surname: String, phones: List[String], salary: Double) {
    def addPhone(number: String): State = copy(phones = phones :+ number)
    def updatePhone(index: Int, number: String): State = copy(phones = phones.updated(index, number))
    def deletePhone(number: String): State = copy(phones = phones.filterNot(_ == number))
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

  case class PhoneAddedEvent(number: String) extends EmployeeEvent
  case class PhoneUpdatedEvent(index: Int, number: String) extends EmployeeEvent
  case class PhoneDeletedEvent(number: String) extends EmployeeEvent
}