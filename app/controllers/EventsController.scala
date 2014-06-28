package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import lastfm.actions._
import play.api.libs.json._
import scala.util.{Success, Failure}

object EventsController extends Controller {
  
  // EXTERNAL ENDPOINTS: ie. used by iPhone application

  // INTERNAL ENDPOINTS: ie. used only by Rails application or testing

  def singleEvent(event_id: Long) = Action.async {
    SingleEvent.get(event_id).map(event => Ok(Json.toJson(event)))
  }

}