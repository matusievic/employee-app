package by.epmloyee.app.actor

import by.epmloyee.app.actor.common.error.AppError

package object common {
  type Result[T] = Either[AppError, T]
}
