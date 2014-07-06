package lastfm.actions

import lastfm.helpers.{UrlBuilder, Pagination}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import lastfm.responses.EventList
import lastfm.entities.Event
import scala.concurrent.Future

object GeoUpcoming extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_lastfm_geo_upcoming")
  def expiry = 1.day

  def get(latitude: Double, longitude: Double, radius: Double, pagination: Pagination = Pagination()): Future[(Int, Seq[Event])] = {
    /*
      2 digits of precision allows for 1.1km of accuracy
      In order to prevent caches never hitting with changing location, round to that precision
    */
    val latRounded: Double = BigDecimal(latitude).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    val longRounded: Double = BigDecimal(longitude).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    val radRounded: Double = radius.round // and this to the nearest whole value
    
    // Get the events in chunks
    val chunkSize = 10
    val chunksPerPage = math.max(1, (pagination.limit.toDouble / chunkSize).ceil.toInt)

    val minPage = 1 + (pagination.page - 1) * chunksPerPage
    val maxPage = minPage + chunksPerPage - 1

    val urls = (minPage to maxPage).map { page =>
      UrlBuilder.geo_getEvents(latRounded, longRounded, radRounded, Pagination(limit = chunkSize, page = page))
    }

    ExternalApiCall.getPar[EventList](urls).map { r =>
      val eventList = r.reduce(EventList.combine)
      (eventList.meta.total, eventList.events)
    }
  }

}