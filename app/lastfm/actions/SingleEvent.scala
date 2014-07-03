package lastfm.actions

import lastfm.UrlBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import lastfm.entities.Event
import scala.concurrent.Future
import scala.util.{Try, Success, Failure}
import lastfm.responses.SingleResponse

object SingleEvent extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("single_events")
  def expiry = 1.day

  def get(eventId: Long): Future[Event] = {
    ExternalApiCall.get[SingleResponse](UrlBuilder.event_getInfo(eventId)).map { r =>
      r.event.get
    }
  }

  def multi(eventIds: Seq[Long]): Future[Seq[Event]] = {
    val urls = eventIds.map(UrlBuilder.event_getInfo(_))

    ExternalApiCall.getPar[Event](urls)
  }

}