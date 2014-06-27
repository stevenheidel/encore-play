package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api._
import play.api.mvc._
import lastfm.actions._
import play.api.libs.json._
import scala.util.{Success, Failure}
import scala.concurrent.Future
import lastfm.entities.GeoPoint

object ArtistsController extends Controller {

  // EXTERNAL ENDPOINTS: ie. used by iPhone application

  def combinedSearch(latitude: Double, longitude: Double, radius: Double, term: String, tense: String) = Action.async {
    for {
      matchingArtists <- ArtistSearch.get(term)
      firstArtist = matchingArtists.head
      artistName = firstArtist.name
      otherArtists = matchingArtists.tail
      events <- if (tense == "past") PastEvents.get(artistName) else FutureEvents.get(artistName)
    } yield {
      val location = GeoPoint(Some(latitude), Some(longitude))
      val filteredEvents = events.filter(_.venue.get.geo_point.distanceTo(location) < 100 * radius)

      Ok(Json.obj(
        "artist" -> Json.toJson(firstArtist),
        "others" -> Json.toJson(otherArtists),
        "events" -> Json.toJson(filteredEvents)
      ))
    }
  }

  def artistPicture(artist_id: String) = Action.async {
    SingleArtist.get(artist_id).map(artist => Ok(Json.obj("image_url" -> artist.largestImage.url)))
  }

  def artistInfo(artist_id: String, limit_events: Int) = Action.async {
    for {
      past <- PastEvents.get(artist_id, limit_events)
      future <- FutureEvents.get(artist_id, limit_events)
    } yield {
      Ok(Json.obj(
        "name" -> artist_id,
        "events" -> Json.obj(
          "past" -> Json.toJson(past),
          "upcoming" -> Json.toJson(future)
        )
      ))
    }
  }

  // INTERNAL ENDPOINTS: ie. used only by Rails application or testing

  def pastEvents(artist_id: String) = Action.async {
    PastEvents.get(artist_id).map(list => Ok(Json.toJson(list)))
  }

}