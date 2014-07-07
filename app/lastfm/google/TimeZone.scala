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

object TimeZone extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_google_timezone")
  def expiry = 1.day

  def currentUnixTime(): Long = {
    DateTime.now.getMillis() / 1000L // Unix time is in seconds, not milliseconds
  }

  val key = Play.current.configuration.getString("google.timezone_key").get

  // Get offset for a latitude/longitude
  def get(latitude: Double, longitude: Double): Future[Int] = {
    val url = "https://maps.googleapis.com/maps/api/timezone/json" ? ("key" -> key) & 
                ("location" -> s"$latitude,$longitude") & 
                ("timestamp" -> currentUnixTime()) // useless because we don't use the dtcOffset anyway
    
    ExternalApiCall.get[JsValue](url).map { json =>
      (json \ "rawOffset").as[Int]
    }
  }

}