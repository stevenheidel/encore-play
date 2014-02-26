package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future
import scala.util.{Success, Failure}

import lastfm.LastfmMethod
import lastfm.UrlBuilder

object Events extends Controller with MongoController with LastfmMethod {

  def collection = db.collection[JSONCollection]("single_events")

  val transformer = (__ \ "event").json.pick

  def get(id: Long) = Action.async {
    val path = UrlBuilder.event_getInfo(id)
    val searchParameters = Json.obj("event.id" -> id.toString)
    val indexParameters = JsNull

    execute(path, searchParameters, indexParameters) match {
      case Success(json) => json.map(Ok(_))
      case Failure(exception) => Future(InternalServerError(exception.toString))
    }
  }

}