package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class GeoPoint(
  lat: Double, 
  long: Double
)

object GeoPoint {
  // Convert from Last.fm format
  implicit val geopointReads: Reads[GeoPoint] = (
    (__ \ "geo:lat").read[String].map(_.toDouble) ~
    (__ \ "geo:long").read[String].map(_.toDouble)
  )(GeoPoint.apply _)
}