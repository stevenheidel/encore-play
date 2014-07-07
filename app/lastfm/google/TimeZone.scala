package lastfm.google

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import com.netaporter.uri._
import com.netaporter.uri.dsl._
import scala.concurrent.Future
import utils.GeoPoint

object TimeZone extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_google_timezone")
  def expiry = 30.days // The world's government's don't change time zones that often

  // This value is ignored anyway, as dtcOffset isn't used. Currently set to January 29, 1991
  val timestamp = 665132523

  val key = Play.current.configuration.getString("google.timezone_key").get

  // Get offset for a latitude/longitude
  def get(latitude: Double, longitude: Double): Future[Int] = {
    // One decimal point is enough to distinguish 11km of accuracy
    val (lat, long) = GeoPoint.round(latitude, longitude, 1)

    val url = "https://maps.googleapis.com/maps/api/timezone/json" ? ("key" -> key) & 
                ("location" -> s"$lat,$long") & 
                ("timestamp" -> timestamp)
    
    ExternalApiCall.get[JsValue](url).map { json =>
      (json \ "rawOffset").as[Int]
    }
  }

}