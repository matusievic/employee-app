package by.epmloyee.app.actor.employee.util

import akka.persistence.PersistentActor
import by.epmloyee.app.actor.employee.util.RestartableActor.{RestartActor, RestartActorException}

trait RestartableActor extends PersistentActor {

  abstract override def receiveCommand = super.receiveCommand orElse {
    case RestartActor => throw RestartActorException
  }
}

object RestartableActor {
  case object RestartActor

  private object RestartActorException extends Exception
}

