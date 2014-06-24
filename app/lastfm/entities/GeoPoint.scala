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
  implicit val geopointReads: Reads[GeoPoint] = (
    (__ \ "geo:lat").read[Option[Double]](toDoubleOption) ~
    (__ \ "geo:long").read[Option[Double]](toDoubleOption)
  )(GeoPoint.apply _)
}