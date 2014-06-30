package controllers

import play.api.mvc._

object RedirectController extends Controller {

  def redirect(path: String) = Action { implicit request =>
    val queryString: String = if (request.rawQueryString.nonEmpty) "?" + request.rawQueryString else ""

    TemporaryRedirect("http://on.encore.fm/api/v1/" + path + queryString )
  }

}