package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import lastfm.traits.HasImages

case class Venue(
  id: Long, 
  name: String, 
  geo_point: GeoPoint, 
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
    (__ \ "geo:point").read[GeoPoint] ~
    (__ \ "city").read[String] ~
    (__ \ "country").read[String] ~
    (__ \ "street").read[String] ~
    (__ \ "postalcode").read[String] ~
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
        "country" -> venue.country
      ) ++ Json.toJson(venue.geo_point).asInstanceOf[JsObject]
    }
  }
}