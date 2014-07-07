package utils

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.math._

case class GeoPoint(lat: Double, long: Double) {
  def distanceTo(other: GeoPoint): Double = {
    // Convert to radians
    val x1 = toRadians(lat)
    val y1 = toRadians(long)
    val x2 = toRadians(other.lat)
    val y2 = toRadians(other.long)

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
  // Filters out objects too far away from a given point
  def nearFilter[T](seq: Seq[T], reference: GeoPoint, distance: Double)(implicit getPoint: T => GeoPoint): Seq[T] = {
    seq.filter(getPoint(_).distanceTo(reference) < distance)
  }

  def round(latitude: Double, longitude: Double, decimals: Int): (Double, Double) = {
    val latRounded: Double = BigDecimal(latitude).setScale(decimals, BigDecimal.RoundingMode.HALF_UP).toDouble
    val longRounded: Double = BigDecimal(longitude).setScale(decimals, BigDecimal.RoundingMode.HALF_UP).toDouble

    (latRounded, longRounded)
  }
}