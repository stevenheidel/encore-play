package populator.instagram.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Meta(
  code: Int,
  error_type: Option[String],
  error_message: Option[String]
)

object Meta {
  implicit val metaReads = Json.reads[Meta]
}