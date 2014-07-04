package users

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._

case class User(
  name: String,
  facebook_id: Long,
  oauth: String,
  expiration_date: String,
  email: Option[String],
  invite_sent: Boolean,
  invite_timestamp: Option[DateTime]
) {
  val facebook_image_url = s"https://graph.facebook.com/$facebook_id/picture?type=large"

  lazy val jsonShort = Json.obj(
    "name" -> name,
    "facebook_image_url" -> facebook_image_url
  )

  lazy val jsonLong = jsonShort ++ Json.obj(
    "facebook_id" -> facebook_id.toString,
    "invite_sent" -> invite_sent
  )
}

object User {
  // Reading from request
  implicit val userReads: Reads[User] = (
    (__ \ "name").read[String] ~
    ((__ \ "facebook_id").read[Long] orElse (__ \ "facebook_id").read[String].map(_.toLong)) ~
    (__ \ "oauth").read[String] ~
    (__ \ "expiration_date").read[String] ~
    (__ \ "email").readNullable[String] ~
    (__ \ "invite_sent").readNullable[Boolean].map(_.getOrElse(false)) ~
    (__ \ "invite_timestamp").readNullable[DateTime]
  )(User.apply _)

  // Used for database
  implicit val userWrites = Json.writes[User]
}