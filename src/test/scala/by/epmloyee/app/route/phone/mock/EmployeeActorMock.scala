package by.epmloyee.app.route.phone.mock

import akka.actor.{Actor, Props}
import by.epmloyee.app.actor.common.error.{InvalidParam, NotFound}
import by.epmloyee.app.actor.employee.EmployeeActor._

class EmployeeActorMock extends Actor {
  val dummyPhone = Phone("1", "1")

  override def receive: Receive = {
    case AddPhone(phone) =>
      sender ! (if (phone != dummyPhone) Right(phone) else Left(InvalidParam("This phone already presented")))
    case ReadAllPhones() =>
      sender ! Right(Seq(dummyPhone))
    case ReadPhone(index) =>
      sender ! (if (index == 0) Right(dummyPhone) else Left(NotFound("Invalid index")))
    case UpdatePhone(index, phone) =>
      sender ! (if (index == 0) Right(phone) else Left(InvalidParam("Invalid index")))
    case DeletePhone(index) =>
      sender ! (if (index == 0) Right(dummyPhone) else Left(NotFound("Invalid index")))
  }
}

object EmployeeActorMock {
  val props: Props = Props(classOf[EmployeeActorMock])
}
