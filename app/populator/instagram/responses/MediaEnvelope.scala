package populator.instagram.responses

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import populator.instagram.entities._

case class MediaEnvelope(
  meta: Meta,
  data: Seq[Media],
  pagination: Pagination
) {
  val media = data
}

object MediaEnvelope {
  implicit val envelopeReads = Json.reads[MediaEnvelope]
}