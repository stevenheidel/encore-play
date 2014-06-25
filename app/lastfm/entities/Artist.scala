package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.Helpers._
import lastfm.traits.HasImages

case class Artist(
  name: String, 
  mbid: String, 
  url: String, 
  image: Seq[Image], 
  streamable: Boolean, 
  ontour: Boolean
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
    (__ \ "ontour").read[Boolean](binaryToBool)
  )(Artist.apply _)
}