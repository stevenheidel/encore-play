package lastfm

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.util.Try
import lastfm.entities.Event
import utils.GeoPoint

object Helpers {
  // Convert a reads to integer, defaulting to 0 if it fails
  def safeToInt(implicit r: Reads[String]): Reads[Int] = r.map(s => Try(s.toInt).getOrElse(0))

  // Convert a reads to long, defaulting to 0 if it fails
  def safeToLong(implicit r: Reads[String]): Reads[Long] = r.map(s => Try(s.toLong).getOrElse(0L))

  // Convert a reads to double, wrapped in the option type
  // ie. None if string was "" or bad number
  def toDoubleOption(implicit r: Reads[String]): Reads[Option[Double]] = r.map(s => Try(s.toDouble).toOption)

  // Convert "0" to false and "1" to true
  def binaryToBool(implicit r: Reads[String]): Reads[Boolean] = r.map {
    case "1" => true
    case _ => false
  }

  // The maximum radius to search on calls with latitude and longitude
  val MaxDistance = 100.0
}