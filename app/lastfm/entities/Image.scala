package lastfm.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Image(
  url: String, 
  size: String
)

object Image {
  // Convert from Last.fm format
  implicit val imageReads: Reads[Image] = (
    (__ \ "#text").read[String] ~
    (__ \ "size").read[String]
  )(Image.apply _)
}