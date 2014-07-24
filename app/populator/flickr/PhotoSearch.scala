package populator.flickr

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import scala.concurrent.Future
import play.api._
import com.netaporter.uri._
import com.netaporter.uri.dsl._

object PhotoSearch extends ExternalApiCache {
  def collection = db.collection[JSONCollection]("cache_flickr_search")
  def expiry = 1.hour

  def convertToUnixTime(date: DateTime): Long = {
    date.getMillis() / 1000L // Unix time is in seconds, not milliseconds
  }

  val key = Play.current.configuration.getString("flickr.key").get
  val baseUrl: Uri = "https://secure.flickr.com/services/rest/" ? 
                        ("format" -> "json") & ("nojsoncallback" -> 1) &
                        ("api_key" -> key) & ("method" -> "flickr.photos.search") &
                        ("extras" -> "owner_name,url_m,url_n,url_z")

  def get(latitude: Double, longitude: Double, startTime: DateTime, endTime: DateTime): Future[Seq[Photo]] = {
    val params = Seq(
      "min_taken_date" -> convertToUnixTime(startTime),
      "max_taken_date" -> convertToUnixTime(endTime),
      "lat" -> latitude,
      "lon" -> longitude,
      "radius" -> 0.2//km
    )
    val url = baseUrl.addParams(params)

    ExternalApiCall.get[PhotoEnvelope](url).map { e =>
      e.photos
    }
  }

  def get(machineTag: String) = {
    val params = Seq(
      "machine_tags" -> machineTag
    )
    val url = baseUrl.addParams(params)

    ExternalApiCall.get[PhotoEnvelope](url).map { e =>
      e.photos
    }
  }
}