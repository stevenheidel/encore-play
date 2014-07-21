package populator.youtube

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Video(
  title: String,
  link: String,
  image_url: String,
  user_name: String,
  description: String
)

object Video {
  implicit val videoReads: Reads[Video] = (
    (__ \ "entry" \ "title" \ "$t").read[String] ~
    ((__ \ "entry" \ "media$group" \ "media$content")(0) \ "url").read[String] ~
    // (2) is the hq default
    ((__ \ "entry" \ "media$group" \ "media$thumbnail")(2) \ "url").read[String] ~
    ((__ \ "entry" \ "author")(0) \ "name" \ "$t").read[String] ~
    (__ \ "entry" \ "media$group" \ "media$description" \ "$t").read[String]
  )(Video.apply _)
}