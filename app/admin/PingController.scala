package admin

import play.api._
import play.api.mvc._

object PingController extends Controller {
  def ping = Action(Ok("pong"))
}