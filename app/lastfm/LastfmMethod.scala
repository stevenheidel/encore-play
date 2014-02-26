package lastfm

import play.api.libs.ws._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.Try

import reactivemongo.api._
import play.modules.reactivemongo.json.collection.JSONCollection

trait LastfmMethod {

  // The MongoDB collection to use for the current method / group of methods
  def collection: JSONCollection

  // The way to change the LastFM response into a suitable Encore response
  val transformer: Reads[JsValue]

  def execute(path: String, searchParameters: JsValue, indexParameters: JsValue): Try[Future[JsValue]] = Try {
    // Prepare the response from Lastfm
    val response: Future[Response] = WS.url(path).get()

    // Check to see if the response is in the database
    val dbCursor: Cursor[JsValue] = collection.find(searchParameters).cursor[JsValue]
    val dbList: Future[List[JsValue]] = dbCursor.collect[List]()

    dbList.flatMap { list =>
      if (list.isEmpty) {
        response.map { result =>
          collection.insert(result.json).map(lastError =>
            // TODO: Log this error
            Json.obj("error" -> lastError.toString))

          result.json
        }
      }
      else {
        Future(list.head)
      }
    }
  }

}