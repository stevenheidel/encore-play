package controllers

import play.api.mvc._

object RedirectController extends Controller {

  def redirect(path: String) = Action { implicit request =>
    Redirect("http://on.encore.fm" + request.uri)
  }

}