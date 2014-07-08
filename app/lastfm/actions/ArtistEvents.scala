package lastfm.actions

import lastfm.helpers.{UrlBuilder, Pagination}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import lastfm.entities.Event
import lastfm.responses.EventList
import scala.concurrent.Future
import scala.util.{Try, Success, Failure}
import com.netaporter.uri._
import lastfm.helpers.XmlConvert

trait ArtistEvents extends ExternalApiCache {

  // Path is the only thing that changes between past and future
  val makePath: (String, Pagination) => Uri

  def get(artistName: String, count: Int = 0): Future[Seq[Event]] = {
    // Get the time to sync all the results
    val currentTime = DateTime.now

    // Start by finding the number of events
    def getNumEvents(): Future[Int] = {
      val path = makePath(artistName, Pagination(limit = 1))

      ExternalApiCall.get[EventList](path).map { r =>
        r.meta.total
      }
    }

    // Get number of events, if specified, otherwise get all of them
    val numEvents: Future[Int] = if (count > 0) Future.successful(count) else getNumEvents()

    // Get the events in chunks
    val chunkSize = 10

    numEvents.flatMap { n =>
      // Get at least one page so can return no result
      val pages = math.max(1, (n.toDouble / chunkSize).ceil.toInt)

      val urls: Seq[Uri] = (1 to pages).map { page =>
        makePath(artistName, Pagination(limit = chunkSize, page = page))
      }

      ExternalApiCall.getPar[EventList](urls).map { r =>
        r.reduce(EventList.combine).events
      }
    }
  }
}

object ArtistPastEvents extends ExternalApiCache with ArtistEvents {

  def collection = db.collection[JSONCollection]("cache_lastfm_artist_past")
  def expiry = 1.day

  // Past event requests are slow
  override val timeout = 15.seconds

  // Past events with ampersands in artist name cause problems
  override val jsonConverter = XmlConvert.convert _

  val makePath = UrlBuilder.artist_getPastEvents _

}

object ArtistFutureEvents extends ExternalApiCache with ArtistEvents {

  def collection = db.collection[JSONCollection]("cache_lastfm_artist_future")
  def expiry = 1.day

  val makePath = UrlBuilder.artist_getEvents _

}