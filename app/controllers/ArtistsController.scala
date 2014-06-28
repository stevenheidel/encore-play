package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api._
import play.api.mvc._
import lastfm.actions._
import play.api.libs.json._
import scala.util.{Success, Failure}
import scala.concurrent.Future
import utils.GeoPoint
import lastfm.entities.Event
import lastfm.Helpers._

object ArtistsController extends Controller {

  // EXTERNAL ENDPOINTS: ie. used by iPhone application

  def combinedSearch(latitude: Double, longitude: Double, radius: Double, term: String, tense: String) = Action.async {
    for {
      matchingArtists <- ArtistSearch.get(term)
      firstArtist = matchingArtists.head
      artistName = firstArtist.name
      otherArtists = matchingArtists.tail
      events <- if (tense == "past") ArtistPastEvents.get(artistName) else ArtistFutureEvents.get(artistName)
    } yield {
      val filteredEvents = GeoPoint.nearFilter[Event](events, GeoPoint(latitude, longitude), radius * MaxDistance)

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
      past <- ArtistPastEvents.get(artist_id, limit_events)
      future <- ArtistFutureEvents.get(artist_id, limit_events)
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

}