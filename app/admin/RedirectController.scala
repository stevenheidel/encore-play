package admin

import play.api._
import play.api.mvc._

object RedirectController extends Controller {

  def redirect(path: String) = Action { request =>
    val queryString: String = if (request.rawQueryString.nonEmpty) "?" + request.rawQueryString else ""
    val url: String = "http://on.encore.fm/api/v1/" + path + queryString

    Logger.debug("Redirected to: " + url)
    TemporaryRedirect(url)
  }

}