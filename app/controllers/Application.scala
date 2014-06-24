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

object Events extends Controller with MongoController with ExternalApiCache {

  def collection = db.collection[JSONCollection]("single_events")
  def expiry = 1.minute

  def get(id: Long) = Action.async {
    val path = UrlBuilder.event_getInfo(id)
    val searchParameters = Json.obj("event_id" -> id.toString)
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