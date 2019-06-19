package by.epmloyee.app.route.directive

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import by.epmloyee.app.actor.common.Result
import by.epmloyee.app.actor.common.error.{AppError, InvalidParam, NotFound}
import by.epmloyee.app.route.json.Json
import io.circe.{Decoder, Encoder}

import scala.concurrent.{ExecutionContext, Future}

object AppDirective extends Directives {
  def handleRequest[Req: Decoder, Resp: Encoder](process: Req => Future[Any])(implicit ec: ExecutionContext, timeout: Timeout): Route = {
    commonHandler { () =>
      entity(as[String]) { body =>
        Json.parse[Req](body).fold[Route](
          Completion.invalidResponse(StatusCodes.BadRequest, _),
          body => Completion.validResponse[Resp](process(body))
        )
      }
    }
  }

  def handleRequest[Resp: Encoder](process: () => Future[Any])(implicit ec: ExecutionContext, timeout: Timeout): Route = {
    commonHandler(() => Completion.validResponse[Resp](process()))
  }

  private def commonHandler(f: () => Route): Route = {
    try {
      f()
    } catch {
      case _: Throwable => Completion.invalidResponse(StatusCodes.UnsupportedMediaType, "Invalid request body")
    }
  }

  private object Completion {
    def invalidResponse(code: StatusCode, message: String): Route = {
      complete(HttpResponse(code, entity = Json.compose[String](message)))
    }

    def validResponse[Resp: Encoder](process: Future[Any])(implicit ec: ExecutionContext): Route = {
      complete(process.mapTo[Result[Resp]].map {
        case Left(error) => mapActorErrorToHttpError(error)
        case Right(data) => HttpResponse(status = StatusCodes.OK, entity = Json.compose[Resp](data))
      })
    }

    private def mapActorErrorToHttpError(error: AppError): HttpResponse = error match {
      case NotFound(message) => HttpResponse(StatusCodes.NotFound, entity = Json.compose(message))
      case InvalidParam(message) => HttpResponse(StatusCodes.BadRequest, entity = Json.compose(message))
    }
  }

}
