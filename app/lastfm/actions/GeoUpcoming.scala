package lastfm.actions

import lastfm.{UrlBuilder, Pagination}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import lastfm.responses.EventList
import lastfm.entities.Event
import scala.concurrent.Future

object GeoUpcoming extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("geo_upcoming")
  def expiry = 1.minute

  def get(latitude: Double, longitude: Double, radius: Double, page: Int, limit: Int): Future[Seq[Event]] = {
    /*
      2 digits of precision allows for 1.1km of accuracy
      In order to prevent caches never hitting with changing location, round to that precision
    */
    val latRounded: Double = BigDecimal(latitude).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    val longRounded: Double = BigDecimal(longitude).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    val radRounded: Double = radius.round // and this to the nearest whole value

    val path = UrlBuilder.geo_getEvents(latitude, longitude, radius, Pagination(limit, page))
    val indexParameters = Json.obj("lat" -> latRounded, "long" -> longRounded, "rad" -> radRounded)
    val searchParameters = indexParameters

    val response = ExternalApiCall(path, indexParameters, searchParameters)
    
    response.get().map(_.as[EventList].events)
  }

}