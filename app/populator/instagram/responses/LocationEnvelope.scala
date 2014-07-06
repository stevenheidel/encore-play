package populator.instagram.responses

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import populator.instagram.entities._

case class LocationEnvelope(
  meta: Meta,
  data: Seq[Location]
) {
  val locations = data
}

object LocationEnvelope {
  implicit val envelopeReads = Json.reads[LocationEnvelope]
}