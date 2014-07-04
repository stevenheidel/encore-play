package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.helpers.JsonConversions._
import lastfm.traits.HasImages

case class Artist(
  name: String, 
  mbid: String, 
  url: String, 
  image: Seq[Image], 
  streamable: Boolean, 
  ontour: Option[Boolean]
  // ignore all these ones
  // stats, similar, tags, bio
) extends HasImages

object Artist {
  // Convert from Last.fm format
  implicit val artistReads: Reads[Artist] = (
    (__ \ "name").read[String] ~
    (__ \ "mbid").read[String] ~
    (__ \ "url").read[String] ~
    (__ \ "image").read[Seq[Image]] ~
    (__ \ "streamable").read[Boolean](binaryToBool) ~
    (__ \ "ontour").readNullable[Boolean](binaryToBool)
  )(Artist.apply _)

  implicit val artistWrites: Writes[Artist] = new Writes[Artist] {
    def writes(artist: Artist): JsValue = {
      Json.obj(
        "name" -> artist.name,
        "lastfm_id" -> artist.name,
        "image_url" -> artist.largestImage.url
      )
    }
  }
}