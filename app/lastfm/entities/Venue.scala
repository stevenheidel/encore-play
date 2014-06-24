package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Venue(
  id: Long, 
  name: String, 
  location: Location, 
  url: String, 
  website: String, 
  phonenumber: String, 
  image: Seq[Image]
)

object Venue {
  // Convert from Last.fm format
  implicit val venueReads: Reads[Venue] = (
    (__ \ "id").read[String].map(_.toLong) ~
    (__ \ "name").read[String] ~
    (__ \ "location").read[Location] ~
    (__ \ "url").read[String] ~
    (__ \ "website").read[String] ~
    (__ \ "phonenumber").read[String] ~
    (__ \ "image").read[Seq[Image]]
  )(Venue.apply _)
}