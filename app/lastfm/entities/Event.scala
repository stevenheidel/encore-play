package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

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
  tickets: String, 
  cancelled: Boolean
)

object Event {
  // Convert from Last.fm format
  implicit val eventReads: Reads[Event] = (
    (__ \ "id").read[String].map(_.toLong) ~ 
    (__ \ "title").read[String] ~
    (__ \ "artists" \ "artist").read[Seq[String]] ~
    (__ \ "artists" \ "headliner").read[String] ~
    (__ \ "venue").readNullable[Venue] ~
    (__ \ "startDate").read[String] ~
    (__ \ "description").read[String] ~
    (__ \ "image").read[Seq[Image]] ~
    (__ \ "attendance").read[String].map(_.toInt) ~
    (__ \ "reviews").read[String].map(_.toInt) ~
    (__ \ "tag").read[String] ~
    (__ \ "url").read[String] ~
    (__ \ "website").read[String] ~
    (__ \ "tickets").read[String] ~
    (__ \ "cancelled").read[String].map { 
      case "1" => true
      case _ => false
    }
  )(Event.apply _)
}