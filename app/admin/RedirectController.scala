package admin

import play.api._
import play.api.mvc._
import com.github.nscala_time.time.Imports._
import play.api.Play.current
import play.api.libs.ws._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object RedirectController extends Controller {

  // If the user tries to go to on.encore.fm redirect to encore.fm instead
  def home = Action {
    Redirect("http://encore.fm")
  }

  // Old /api/v1 methods
  def deprecated(path: String) = Action.async { request =>
    val correctedPath = if (path.endsWith(".json")) path else path + ".json"
    val correctedQuery = if (request.rawQueryString.nonEmpty) "?" + request.rawQueryString + s"&date=${DateTime.now}" else ""

    val corrected = WS.url("http://" + request.host + "/api/v2/" + correctedPath + correctedQuery)

    val correctedBody = request.body.asFormUrlEncoded.getOrElse(Map("fake" -> Seq("fake")))

    val response = request.method match {
      case "GET" => corrected.get()
      case "POST" => corrected.post(correctedBody)
      case "PATCH" => corrected.patch(correctedBody)
      case "DELETE" => corrected.delete()
    }

    response.map(r => Ok(r.json))
  }

}