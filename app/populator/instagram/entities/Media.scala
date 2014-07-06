package populator.instagram.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Media(
  id: String,
  typ: String,
  caption: Option[String],
  link: String,
  image_url: String,
  user_name: String,
  user_profile_picture: String,
  user_id: String
)

object Media {
  implicit val mediaReads: Reads[Media] = (
    (__ \ "id").read[String] ~
    (__ \ "type").read[String] ~
    (__ \ "caption" \ "text").readNullable[String] ~
    (__ \ "link").read[String] ~
    (__ \ "images" \ "standard_resolution" \ "url").read[String] ~
    (__ \ "user" \ "username").read[String] ~
    (__ \ "user" \ "profile_picture").read[String] ~
    (__ \ "user" \ "id").read[String]
  )(Media.apply _)
}