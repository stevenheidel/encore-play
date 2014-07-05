package populator.instagram.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Pagination(
  next_url: String,
  next_max_id: String
)

object Pagination {
  implicit val paginationReads = Json.reads[Pagination]
}