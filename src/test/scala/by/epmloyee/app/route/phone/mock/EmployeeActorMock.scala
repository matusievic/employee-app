package by.epmloyee.app.route.phone.mock

import akka.actor.{Actor, Props}
import by.epmloyee.app.actor.employee.EmployeeActor._

class EmployeeActorMock extends Actor {
  override def receive: Receive = {
    case AddPhoneRequest(number) =>
      sender ! AddPhoneResponse(if (number != "1") Some(number) else None)
    case ReadAllPhonesRequest() =>
      sender ! ReadAllPhonesResponse(Seq("1"))
    case ReadPhoneRequest(index) =>
      sender ! ReadPhoneResponse(if (index == 0) Some("1") else None)
    case UpdatePhoneRequest(index, number) =>
      sender ! UpdatePhoneResponse(if (index == 0) Some(number) else None)
    case DeletePhoneRequest(index) =>
      sender ! DeletePhoneResponse(if (index == 0) Some("1") else None)
  }
}

object EmployeeActorMock {
  val props: Props = Props(new EmployeeActorMock())
}
