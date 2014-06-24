package controllers

import lastfm.UrlBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import lastfm.entities.Event
import lastfm.collections.PastEvents
import scala.concurrent.Future

object Single extends Controller with MongoController with ExternalApiCache {

  def collection = db.collection[JSONCollection]("single_events")
  def expiry = 1.minute

  def get(event_id: Long) = Action.async {
    val path = UrlBuilder.event_getInfo(event_id)
    val searchParameters = Json.obj("event_id" -> event_id.toString)
    val indexParameters = searchParameters

    val response = ExternalApiCall(path, searchParameters, indexParameters)
    
    response.get().map(json =>
      (json \ "event").validate[Event] match {
        case s: JsSuccess[Event] => {
          val event = s.get
          println(event)
          Ok(json)
        }
        case e: JsError => {
          InternalServerError(JsError.toFlatJson(e).toString())
        }
      }
    )
  }

}

object Past extends Controller with MongoController with ExternalApiCache {

  def collection = db.collection[JSONCollection]("past_events")
  def expiry = 1.minute

  def get(artist_id: String) = Action.async {
    // Get the time to sync all the results
    val currentTime = DateTime.now

    // Start by finding the number of events
    val path = UrlBuilder.artist_getPastEvents(artist_id, limit = 1)
    val searchParameters = Json.obj("artist_id" -> artist_id, "page" -> 1, "limit" -> 1)
    val indexParameters = searchParameters

    val response = ExternalApiCall(path, searchParameters, indexParameters, currentTime)

    val numEvents: Future[Int] = response.get().map(json =>
      json.validate[PastEvents] match {
        case s: JsSuccess[PastEvents] => s.get.total
        case e: JsError => 0
      }
    )

    // Get the events in chunks
    val chunkSize = 10

    numEvents.flatMap { n =>
      val pages = (n / chunkSize).ceil.toInt

      val futures: Seq[Future[Seq[Event]]] = (1 to pages).map { page =>
        val path = UrlBuilder.artist_getPastEvents(artist_id, limit = chunkSize, page = page)
        val searchParameters = Json.obj("artist_id" -> artist_id, "page" -> page, "limit" -> chunkSize)
        val indexParameters = searchParameters

        ExternalApiCall(path, searchParameters, indexParameters, currentTime).get().map(json =>
          json.validate[PastEvents] match {
            case s: JsSuccess[PastEvents] => s.get.events
            case e: JsError => Seq()
          }
        )
      }.toList

      val future: Future[Seq[Seq[Event]]] = Future.sequence(futures)

      val eventList: Future[Seq[Event]] = future.map(_.flatten)

      eventList.map(e => Ok(e.toString))
    }
  }

}