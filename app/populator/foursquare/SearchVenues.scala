package populator.foursquare

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import scala.concurrent.Future
import com.netaporter.uri._
import com.netaporter.uri.dsl._

object SearchVenues extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_foursquare_location_search")
  def expiry = 1.day

  // Returns a list of Foursquare Venue IDs
  def get(venueName: String, latitude: Double, longitude: Double): Future[Seq[String]] = {
    val params = Seq(
      "client_id" -> Play.current.configuration.getString("foursquare.client_id").get,
      "client_secret" -> Play.current.configuration.getString("foursquare.client_secret").get,
      "v" -> 20140101, // The Foursquare API version
      "ll" -> s"$latitude,$longitude",
      "query" -> venueName,
      "intent" -> "match"
    )

    val url = ("https://api.foursquare.com/v2" / "venues/search").addParams(params)

    ExternalApiCall.get[JsValue](url).map { json => 
      (json \ "response" \ "venues" \\ "id").map(_.as[String])
    }
  }

}