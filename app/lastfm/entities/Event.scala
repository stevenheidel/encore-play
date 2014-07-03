package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import lastfm.traits.HasImages
import utils.GeoPoint
import scala.language.implicitConversions
import com.github.nscala_time.time.Imports._

case class Event(
  id: Long, 
  title: String, 
  artists: Seq[String],
  headliner: String,
  venue: Option[Venue], 
  startDate: String, // Stores local time in format: "Mon, 07 Apr 2014 19:30:00"
  description: String, 
  image: Seq[Image], 
  attendance: Int, 
  reviews: Int, 
  tag: String, 
  url: String, 
  website: String, 
  tickets: Option[String], 
  cancelled: Boolean
) extends HasImages {
  // Returns local date in format: "2014-04-07"
  val justDate: String = {
    val readFormat = new java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss")
    val writeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")

    val date = readFormat.parse(startDate)
    writeFormat.format(date)
  }

  // Takes a date in ISO 8601 and determines if it is the same day
  def isToday(date: String) = {
    justDate == date.take("yyyy-MM-dd".length)
  }

  // Lineup should have the headliner first, then everyone else with no duplicates
  val lineup: Seq[String] = (headliner +: artists).distinct

  /* Tickets URL Algorithm
      If "website" URL available, use (no text analysis).
      Else, if "tickets" has URL, use. [haven't seen one in these tags at all, although slavik said 10% did when he checked]
      Else, check "description": a. if only one URL exists, use. b. if more than 1 URL exists, use the text analysis script, use best match.
      Else, use the URL in "venue""website" (not "venue""url")
      Else, use the last.fm URL
  */
  val ticketsUrl: String = {
    // from http://www.java-tutorial.ch/core-java-tutorial/extract-urls-using-java-regular-expressions
    val pattern = "(https?:((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"

    lazy val urlsInDescription: Seq[String] = pattern.r.findAllIn(description).toSeq

    if (website matches pattern) website
    else if (tickets.getOrElse("") matches pattern) tickets.get
    else if (urlsInDescription.nonEmpty) urlsInDescription.head
    else if (venue.map(_.website).getOrElse("") matches pattern) venue.get.website
    else url
  }
}

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
        "date" -> event.justDate,
        "start_time" -> event.startDate,
        "image_url" -> event.largestImage.url,
        "lastfm_url" -> event.url,
        "tickets_url" -> event.ticketsUrl,
        "venue_name" -> event.venue.map(_.name),
        "venue" -> Json.toJson(event.venue),
        "headliner" -> event.headliner,
        "artists" -> event.lineup.map(artist => Json.obj("artist" -> artist))
      )
    }
  }

  // Takes an event and returns its latitude/longitude as a gepoint
  implicit def geoPoint(event: Event): GeoPoint = {
    // If missing information, use South Pole so Event is filtered out of results
    val lat = event.venue.flatMap(_.lat).getOrElse(-90.0)
    val long = event.venue.flatMap(_.long).getOrElse(0.0)

    GeoPoint(lat, long)
  }
}