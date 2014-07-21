import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError("Internal Server Error: " + ex))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound("Not Found: " + request.path))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest("Bad Request: " + error))
  }

}