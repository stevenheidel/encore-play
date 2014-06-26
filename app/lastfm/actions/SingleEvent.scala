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

object SingleEvent extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("single_events")
  def expiry = 1.minute

  def get(event_id: Long): Future[Event] = {
    val path = UrlBuilder.event_getInfo(event_id)
    val searchParameters = Json.obj("event_id" -> event_id.toString)
    val indexParameters = searchParameters

    val response = ExternalApiCall(path, searchParameters, indexParameters)
    
    response.get().map(json => (json \ "event").as[Event])
  }

}