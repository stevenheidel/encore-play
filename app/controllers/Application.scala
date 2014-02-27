package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import lastfm.UrlBuilder
import utils.ExternalApiCache

object Events extends Controller with MongoController with ExternalApiCache {

  def collection = db.collection[JSONCollection]("single_events")

  def get(id: Long) = Action.async {
    val path = UrlBuilder.event_getInfo(id)
    val searchParameters = Json.obj("event.id" -> id.toString)
    val indexParameters = JsNull

    val response = new ExternalApiCall(path, searchParameters, indexParameters)
    response.get().map(Ok(_))
  }

}