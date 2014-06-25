package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import lastfm.actions._
import scala.util.{Success, Failure}

object Application extends Controller {

  def singleEvent(event_id: Long) = Action.async {
    SingleEvent.get(event_id).map {
      case Success(event) => Ok(event.toString())
      case Failure(err) => InternalServerError(err.toString())
    }
  }

  def pastEvents(artist_id: String) = Action.async {
    PastEvents.get(artist_id).map(list => Ok(list.toString()))
  }

}