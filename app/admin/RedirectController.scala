package admin

import play.api._
import play.api.mvc._

object RedirectController extends Controller {

  // If the user tries to go to on.encore.fm redirect to encore.fm instead
  def home = Action {
    Redirect("http://encore.fm")
  }

}