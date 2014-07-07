package populator.instagram.actions

import populator.instagram.helpers.UrlBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import scala.concurrent.Future
import populator.instagram.responses.LocationEnvelope
import populator.instagram.entities.Location
import utils.GeoPoint

object LocationSearch extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_instagram_location_search")
  def expiry = 30.days // These are also unlikely to be added that often

  def get(latitude: Double, longitude: Double): Future[Seq[Location]] = {
    // iPhones will never get more than 5 digits of accuracy anyways
    val (lat, long) = GeoPoint.round(latitude, longitude, 5)

    ExternalApiCall.get[LocationEnvelope](UrlBuilder.location_search(lat, long)).map { r =>
      r.locations
    }
  }

  def get(foursquareId: String): Future[Seq[Location]] = {
    ExternalApiCall.get[LocationEnvelope](UrlBuilder.foursquare_location(foursquareId)).map { r =>
      r.locations
    }
  }

}