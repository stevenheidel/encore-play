package lastfm.responses

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import lastfm.entities.Event

case class EventList(
  events: Seq[Event],
  // if it's an artist request, then artist will be Some() else location will be Some()
  artist: Option[String],
  location: Option[String],
  festivalsonly: Boolean, 
  url: Option[String], 
  page: Int, 
  perPage: Int, 
  totalPages: Int, 
  total: Int
)

object EventList {
  // Convert from Last.fm format
  implicit val pastEventListReads: Reads[EventList] = (
    // If it's a single object, convert to a sequence of one item
    ((__ \ "events" \ "event").read[Seq[Event]] orElse (__ \ "events" \ "event").read[Event].map(Seq(_))) ~
    (__ \ "events" \ "@attr" \ "artist").readNullable[String] ~
    (__ \ "events" \ "@attr" \ "location").readNullable[String] ~
    (__ \ "events" \ "@attr" \ "festivalsonly").read[Boolean](binaryToBool) ~
    (__ \ "events" \ "@attr" \ "url").readNullable[String] ~
    (__ \ "events" \ "@attr" \ "page").read[Int](safeToInt) ~
    (__ \ "events" \ "@attr" \ "perPage").read[Int](safeToInt) ~
    (__ \ "events" \ "@attr" \ "totalPages").read[Int](safeToInt) ~
    (__ \ "events" \ "@attr" \ "total").read[Int](safeToInt)
  )(EventList.apply _)

  // Combine two EventLists
  // Note that all the values other than .events are now worthless, be careful
  def combine(e1: EventList, e2: EventList): EventList = {
    e1.copy(events = e1.events ++ e2.events)
  }
}