package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import scala.math._

case class GeoPoint(
  lat: Option[Double],
  long: Option[Double]
) {
  def distanceTo(other: GeoPoint): Double = {
    // If no distance, then use south pole to filter it out as a result
    val x1 = toRadians(lat.getOrElse(-90.0))
    val y1 = toRadians(long.getOrElse(0.0))

    val x2 = toRadians(other.lat.getOrElse(-90.0))
    val y2 = toRadians(other.long.getOrElse(0.0))

    val sec1 = sin(x1) * sin(x2)
    val dl = abs(y1 - y2)
    val sec2 = cos(x1) * cos(x2)
    // sec1,sec2,dl are in degree, need to convert to radians
    val centralAngle = acos(sec1 + sec2 * cos(dl))
    //Radius of Earth: 6378.1 kilometers
    val distance = centralAngle * 6378.1

    distance
  }
}

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