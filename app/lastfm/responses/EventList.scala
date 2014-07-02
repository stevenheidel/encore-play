package lastfm.responses

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import lastfm.entities.Event

case class EventList(
  events: Seq[Event],
  meta: ListInfo
)

object EventList {
  // Convert from Last.fm format
  implicit val pastEventListReads: Reads[EventList] = (
    (
      // If it's a single object, convert to a sequence of one item
      (__ \ "events" \ "event").read[Seq[Event]] orElse 
      (__ \ "events" \ "event").read[Event].map(Seq(_)) orElse
      // orElse there are no events at all, just #text
      (__ \ "events" \ "#text").read[String].map(x => Seq())
    ) ~
    // Either there are events orElse there are none, in which case attributes are not under "@attr"
    ((__ \ "events" \ "@attr").read[ListInfo] orElse (__ \ "events").read[ListInfo])
  )(EventList.apply _)

  // Combine two EventLists
  // Note that all the values other than .events are now worthless, be careful
  def combine(e1: EventList, e2: EventList): EventList = {
    e1.copy(events = e1.events ++ e2.events)
  }
}

case class ListInfo(
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

object ListInfo {
  implicit val listInfoReads: Reads[ListInfo] = (
    (__ \ "artist").readNullable[String] ~
    (__ \ "location").readNullable[String] ~
    (__ \ "festivalsonly").read[Boolean](binaryToBool) ~
    (__ \ "url").readNullable[String] ~
    (__ \ "page").read[Int](safeToInt) ~
    (__ \ "perPage").read[Int](safeToInt) ~
    (__ \ "totalPages").read[Int](safeToInt) ~
    (__ \ "total").read[Int](safeToInt)
  )(ListInfo.apply _)
}