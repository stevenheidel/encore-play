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

object LocationSearch extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_instagram_location_search")
  def expiry = 1.day

  def get(latitude: Double, longitude: Double): Future[Seq[Location]] = {
    ExternalApiCall.get[LocationEnvelope](UrlBuilder.location_search(latitude, longitude)).map { r =>
      r.locations
    }
  }

  def get(foursquareId: String): Future[Seq[Location]] = {
    ExternalApiCall.get[LocationEnvelope](UrlBuilder.foursquare_location(foursquareId)).map { r =>
      r.locations
    }
  }

}