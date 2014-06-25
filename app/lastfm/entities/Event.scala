package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import lastfm.traits.HasImages

case class Event(
  id: Long, 
  title: String, 
  artists: Seq[String],
  headliner: String,
  venue: Option[Venue], 
  startDate: String, 
  description: String, 
  image: Seq[Image], 
  attendance: Int, 
  reviews: Int, 
  tag: String, 
  url: String, 
  website: String, 
  tickets: Option[String], 
  cancelled: Boolean
) extends HasImages

object Event {
  // Convert from Last.fm format
  implicit val eventReads: Reads[Event] = (
    (__ \ "id").read[Long](safeToLong) ~ 
    (__ \ "title").read[String] ~
    // If it's a single object, convert to a sequence of one item
    ((__ \ "artists" \ "artist").read[Seq[String]] orElse (__ \ "artists" \ "artist").read[String].map(Seq(_))) ~
    (__ \ "artists" \ "headliner").read[String] ~
    (__ \ "venue").readNullable[Venue] ~
    (__ \ "startDate").read[String] ~
    (__ \ "description").read[String] ~
    (__ \ "image").read[Seq[Image]] ~
    (__ \ "attendance").read[Int](safeToInt) ~
    (__ \ "reviews").read[Int](safeToInt) ~
    (__ \ "tag").read[String] ~
    (__ \ "url").read[String] ~
    (__ \ "website").read[String] ~
    (__ \ "tickets").readNullable[String] ~
    (__ \ "cancelled").read[Boolean](binaryToBool)
  )(Event.apply _)

  implicit val eventWrites: Writes[Event] = new Writes[Event] {
    def writes(event: Event): JsValue = {
      Json.obj(
        "lastfm_id" -> event.id.toString(),
        "name" -> event.title,
        "date" -> JsNull,
        "start_time" -> JsNull,
        "image_url" -> event.largestImage.url,
        "lastfm_url" -> event.url,
        "tickets_url" -> event.tickets,
        "venue_name" -> event.venue.map(_.name),
        "venue" -> Json.toJson(event.venue),
        "headliner" -> event.headliner,
        "artists" -> event.artists.map(artist => Json.obj("artist" -> artist))
      )
    }
  }
}