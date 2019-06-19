package by.epmloyee.app.route.json

import by.epmloyee.app.actor.employee.EmployeeActor.Phone
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

trait JsonImplicits {
  implicit val phoneEncoder: Encoder[Phone] = deriveEncoder
  implicit val phoneDecoder: Decoder[Phone] = deriveDecoder
  implicit val phoneListEncoder: Encoder[List[Phone]] = deriveEncoder
}
