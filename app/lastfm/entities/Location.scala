package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Location(
  geo_point: GeoPoint, 
  city: String, 
  country: String, 
  street: String, 
  postalcode: String
)

object Location {
  // Convert from Last.fm format
  implicit val locationReads: Reads[Location] = (
    (__ \ "geo:point").read[GeoPoint] ~
    (__ \ "city").read[String] ~
    (__ \ "country").read[String] ~
    (__ \ "street").read[String] ~
    (__ \ "postalcode").read[String]
  )(Location.apply _)

  implicit val locationWrites: Writes[Location] = new Writes[Location] {
    def writes(location: Location): JsValue = {
      Json.obj(
        "street" -> location.street,
        "city" -> location.city,
        "postalcode" -> location.postalcode,
        "country" -> location.country
      ) ++ Json.toJson(location.geo_point).asInstanceOf[JsObject]
    }
  }
}