package populator.instagram.actions

import populator.instagram.helpers.UrlBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import scala.concurrent.Future
import populator.instagram.responses.MediaEnvelope
import populator.instagram.entities.{Media, Location}

object LocationRecentMedia extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_instagram_location_media")
  def expiry = 1.day

  def convertToUnixTime(date: DateTime): Long = {
    date.getMillis() / 1000L // Unix time is in seconds, not milliseconds
  }

  def get(location: Location, min_time: DateTime, max_time: DateTime, max_id: Option[String] = None): Future[MediaEnvelope] = {
    val url = UrlBuilder.location_recent_media(location.id, convertToUnixTime(min_time), convertToUnixTime(max_time), max_id)

    ExternalApiCall.get[MediaEnvelope](url)
  }

}