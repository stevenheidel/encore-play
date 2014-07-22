import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import play.api.libs.json._

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable) = {
    val msg: String = "Internal Server Error: " + ex
    Future.successful(InternalServerError(Json.obj("error" -> msg)))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    val msg: String = "Not Found: " + request.path
    Future.successful(NotFound(Json.obj("error" -> msg)))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    val msg: String = "Bad Request: " + error
    Future.successful(BadRequest(Json.obj("error" -> msg)))
  }

}