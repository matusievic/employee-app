package by.epmloyee.app.route.phone.mock

import akka.actor.{Actor, Props}
import by.epmloyee.app.actor.employee.EmployeeActor._

class EmployeeActorMock extends Actor {
  val dummyPhone = Phone("1", "1")

  override def receive: Receive = {
    case AddPhoneRequest(phone) =>
      sender ! AddPhoneResponse(if (phone != dummyPhone) Some(phone) else None)
    case ReadAllPhonesRequest() =>
      sender ! ReadAllPhonesResponse(Seq(dummyPhone))
    case ReadPhoneRequest(index) =>
      sender ! ReadPhoneResponse(if (index == 0) Some(dummyPhone) else None)
    case UpdatePhoneRequest(index, phone) =>
      sender ! UpdatePhoneResponse(if (index == 0) Some(phone) else None)
    case DeletePhoneRequest(index) =>
      sender ! DeletePhoneResponse(if (index == 0) Some(dummyPhone) else None)
  }
}

object EmployeeActorMock {
  val props: Props = Props(classOf[EmployeeActorMock])
}
