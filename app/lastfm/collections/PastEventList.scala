package lastfm.collections

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import lastfm.entities.Event

case class PastEventList(
  events: Seq[Event],
  artist: String, 
  festivalsonly: Boolean, 
  url: String, 
  page: Int, 
  perPage: Int, 
  totalPages: Int, 
  total: Int
)

object PastEventList {
  // Convert from Last.fm format
  implicit val pastEventListReads: Reads[PastEventList] = (
    // If it's a single object, convert to a sequence of one item
    ((__ \ "events" \ "event").read[Seq[Event]] orElse (__ \ "events" \ "event").read[Event].map(Seq(_))) ~
    (__ \ "events" \ "@attr" \ "artist").read[String] ~
    (__ \ "events" \ "@attr" \ "festivalsonly").read[Boolean](binaryToBool) ~
    (__ \ "events" \ "@attr" \ "url").read[String] ~
    (__ \ "events" \ "@attr" \ "page").read[Int](safeToInt) ~
    (__ \ "events" \ "@attr" \ "perPage").read[Int](safeToInt) ~
    (__ \ "events" \ "@attr" \ "totalPages").read[Int](safeToInt) ~
    (__ \ "events" \ "@attr" \ "total").read[Int](safeToInt)
  )(PastEventList.apply _)
}