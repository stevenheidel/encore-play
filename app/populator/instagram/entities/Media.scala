package populator.instagram.entities

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Media(
  id: String,
  typ: String,
  caption: String,
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
    (__ \ "caption").read[String] ~
    (__ \ "link").read[String] ~
    (__ \ "image_url").read[String] ~
    (__ \ "user_name").read[String] ~
    (__ \ "user_profile_picture").read[String] ~
    (__ \ "user_id").read[String]
  )(Media.apply _)
}