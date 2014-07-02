package lastfm.responses

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import lastfm.entities.Artist
import lastfm.entities.Event

case class SingleResponse(
  artist: Option[Artist],
  event: Option[Event]
)

object SingleResponse {
  // Convert from Last.fm format
  implicit val singleResponseReads: Reads[SingleResponse] = (
    (__ \ "artist").readNullable[Artist] ~
    (__ \ "event").readNullable[Event]
  )(SingleResponse.apply _)
}