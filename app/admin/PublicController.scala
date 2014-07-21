package admin

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

object PublicController extends Controller {
  def events(event_id: Long) = Action {
    Ok(views.html.public.events(event_id))
  }

  def posts(post_id: String) = Action {
    Ok(views.html.public.posts(post_id))
  }
}