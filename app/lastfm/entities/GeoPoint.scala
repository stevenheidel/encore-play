package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._

case class GeoPoint(
  lat: Option[Double],
  long: Option[Double]
)

object GeoPoint {
  // Convert from Last.fm format
  implicit val geoPointReads: Reads[GeoPoint] = (
    (__ \ "geo:lat").read[Option[Double]](toDoubleOption) ~
    (__ \ "geo:long").read[Option[Double]](toDoubleOption)
  )(GeoPoint.apply _)

  implicit val geoPointWrites: Writes[GeoPoint] = new Writes[GeoPoint] {
    def writes(geoPoint: GeoPoint): JsValue = {
      Json.obj(
        "latitude" -> geoPoint.lat,
        "longitude" -> geoPoint.long
      )
    }
  }
}