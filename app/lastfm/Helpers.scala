package lastfm

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object Helpers {
  // Convert a reads to integer, defaulting to 0 if it fails
  def safeToInt(implicit r: Reads[String]): Reads[Int] = r.map { s =>
    try {
      s.toInt
    } catch {
      case _: NumberFormatException => 0
    }
  }

  // Convert a reads to long, defaulting to 0 if it fails
  def safeToLong(implicit r: Reads[String]): Reads[Long] = r.map { s =>
    try {
      s.toLong
    } catch {
      case _: NumberFormatException => 0
    }
  }

  // Convert a reads to double, wrapped in the option type
  // ie. None if string was "" or bad number
  def toDoubleOption(implicit r: Reads[String]): Reads[Option[Double]] = r.map { s =>
    try {
      Some(s.toDouble)
    } catch {
      case _: NumberFormatException => None
    }
  }

  // Convert "0" to false and "1" to true
  def binaryToBool(implicit r: Reads[String]): Reads[Boolean] = r.map {
    case "1" => true
    case _ => false
  }
}