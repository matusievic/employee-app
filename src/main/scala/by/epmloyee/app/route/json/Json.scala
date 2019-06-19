package by.epmloyee.app.route.json

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, ResponseEntity}
import io.circe.parser.decodeAccumulating
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

object Json {
  def compose[T: Encoder](raw: T): ResponseEntity = {
    HttpEntity(raw.asJson.noSpaces).withContentType(ContentTypes.`application/json`)
  }

  def parse[T: Decoder](raw: String): Either[String, T] = {
    decodeAccumulating[T](raw).toEither match {
      case Left(e) => Left(e.toString())
      case Right(r) => Right(r)
    }
  }
}
