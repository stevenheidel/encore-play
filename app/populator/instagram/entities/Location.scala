package populator.instagram.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Location(
  id: String,
  name: String,
  latitude: Double,
  longitude: Double
)

object Location {
  implicit val readsLocation = Json.reads[Location]
}