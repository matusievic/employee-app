package by.epmloyee.app.actor.common

package object error {

  trait AppError {
    def message: String
  }

  case class NotFound(message: String = "Not Found") extends AppError

  case class InvalidParam(message: String = "Bad Request") extends AppError

  case class UnknownError(message: String = "Internal Server Error") extends AppError

}
