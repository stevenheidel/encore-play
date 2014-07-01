package models

import play.api.libs.json.Json

case class User(
  name: String,
  facebook_id: String,
  oauth: String,
  expiration_date: String,
  email: Option[String]
) {
  lazy val facebook_image_url = s"https://graph.facebook.com/$facebook_id/picture?type=large"
}

object User {
  implicit val userFormat = Json.format[User]
}