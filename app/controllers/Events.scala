package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.concurrent.Future

// Reactive Mongo imports
import reactivemongo.api._

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import lastfm.UrlBuilder

object Events extends Controller with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("responses")

  def get(id: Long) = {
    // Prepare the response from Lastfm
    val response: Future[Response] = WS.url(UrlBuilder.event_getInfo(id)).get()

    // Check to see if the response is in the database
    val dbCursor: Cursor[JsObject] = collection.find(Json.obj("event.id" -> id.toString)).cursor[JsObject]
    val dbList: Future[List[JsObject]] = dbCursor.collect[List]()

    Action.async {
      dbList.flatMap { list =>
        if (list.isEmpty) {
          response.map { result =>
            collection.insert(result.json).map(lastError =>
              Ok("Mongo LastError: %s".format(lastError)))

            Ok(result.json)
          }
        }
        else {
          Future(Ok(list.head))
        }
      }
    }
  }

}