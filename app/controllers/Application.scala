package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import lastfm.actions._
import play.api.libs.json._
import scala.util.{Success, Failure}

object Application extends Controller {

  def artistPicture(artist_id: String) = Action.async {
    SingleArtist.get(artist_id).map(artist => Ok(Json.obj("image_url" -> artist.largestImage.url)))
  }

  def singleEvent(event_id: Long) = Action.async {
    SingleEvent.get(event_id).map(event => Ok(event.toString()))
  }

  def pastEvents(artist_id: String) = Action.async {
    PastEvents.get(artist_id).map(list => Ok(Json.toJson(list)))
  }

}