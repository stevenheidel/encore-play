package populator.instagram.responses

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import populator.instagram.entities._

case class Envelope(
  meta: Meta,
  data: Seq[Location],
  pagination: Pagination
)

object Envelope {
  implicit val envelopeReads = Json.reads[Envelope]
}