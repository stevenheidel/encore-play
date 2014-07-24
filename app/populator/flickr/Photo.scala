package populator.flickr

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class PhotoEnvelope(
  photos: Seq[Photo],
  total: Int
)

object PhotoEnvelope {
  implicit val photoEnvelopeReads: Reads[PhotoEnvelope] = (
    (__ \ "photos" \ "photo").read[Seq[Photo]] ~
    (__ \ "photos" \ "total").read[Int](lastfm.helpers.JsonConversions.safeToInt)
  )(PhotoEnvelope.apply _)
}

case class Photo(
  title: String,
  image_url: String,
  user_name: String
)

object Photo {
  implicit val photoReads: Reads[Photo] = (
    (__ \ "title").read[String] ~
    // Try to get different sizes from largest to smallest
    // See documentation on URLs here: https://secure.flickr.com/services/api/misc.urls.html
    ((__ \ "url_z").read[String] orElse (__ \ "url_n").read[String] orElse (__ \ "url_m").read[String]) ~
    (__ \ "ownername").read[String]
  )(Photo.apply _)
}