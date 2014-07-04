package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.helpers.JsonConversions._
import lastfm.traits.HasImages

case class Venue(
  id: Long, 
  name: String, 
  lat: Option[Double],
  long: Option[Double],
  city: String, 
  country: String, 
  street: String, 
  postalcode: String,
  url: String, 
  website: String, 
  phonenumber: String, 
  image: Seq[Image]
) extends HasImages

object Venue {
  // Convert from Last.fm format
  implicit val venueReads: Reads[Venue] = (
    (__ \ "id").read[Long](safeToLong) ~
    (__ \ "name").read[String] ~
    (__ \ "location" \ "geo:point" \ "geo:lat").read[Option[Double]](toDoubleOption) ~
    (__ \ "location" \ "geo:point" \ "geo:long").read[Option[Double]](toDoubleOption) ~
    (__ \ "location" \ "city").read[String] ~
    (__ \ "location" \ "country").read[String] ~
    (__ \ "location" \ "street").read[String] ~
    (__ \ "location" \ "postalcode").read[String] ~
    (__ \ "url").read[String] ~
    (__ \ "website").read[String] ~
    (__ \ "phonenumber").read[String] ~
    (__ \ "image").read[Seq[Image]]
  )(Venue.apply _)

  implicit val venueWrites: Writes[Venue] = new Writes[Venue] {
    def writes(venue: Venue): JsValue = {
      Json.obj(
        "street" -> venue.street,
        "city" -> venue.city,
        "postalcode" -> venue.postalcode,
        "country" -> venue.country,
        "latitude" -> venue.lat,
        "longitude" -> venue.long
      )
    }
  }
}