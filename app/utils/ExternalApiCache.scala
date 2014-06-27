package utils

import org.joda.time.DateTime
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.Play.current
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import com.github.nscala_time.time.Imports._
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import play.modules.reactivemongo.ReactiveMongoPlugin
import com.netaporter.uri._
import java.util.concurrent.TimeoutException

trait ExternalApiCache {

  def db = ReactiveMongoPlugin.db

  // The MongoDB collection to use for the current method / group of methods
  def collection: JSONCollection
  def expiry: Period
  //def verify

  val timeout = 10.seconds

  collection.indexesManager.ensure(Index(
    key = Seq("_createdAt" -> IndexType.Ascending),
    options = BSONDocument("expireAfterSeconds" -> expiry.seconds)
  ))

  case class ExternalApiCall(
    path: Uri, // the URL of the web JSON to retrieve
    searchParameters: JsObject,
    indexParameters: JsObject,
    date: DateTime = DateTime.now// can be used to sync caches so that multiple expire at the same time
  ) {

    // Check to see if the response is in the database
    private def checkCache: Future[Option[JsValue]] = {
      val dbCursor: Cursor[JsValue] = collection.find(searchParameters).cursor[JsValue]
      val dbList: Future[List[JsValue]] = dbCursor.collect[List]()

      // Just get first record if multiple are found
      dbList.map {
        case Nil => None
        // Get the response itself, not the whole document
        case x :: xs => Some(x \ "_response")
      }
    }

    // Get the response from external API and then cache it to the database
    private def getResponseAndCache: Future[JsValue] = {
      Logger.info("Called External API")
      
      val request = WS.url(path.toString()).withRequestTimeout(timeout.millis.toInt).get()

      request.map { externalResponse =>
        val externalJson = externalResponse.json

        // TODO: Verify response here

        // Save raw response along with some indexing parameters in order to find it later
        val extraRecords = Json.obj(
          "_response" -> externalJson, 
          "_url" -> path.toString(), 
          "_createdAt" -> Json.obj("$date" -> date.getMillis)
        )
        val databaseRecord = indexParameters ++ extraRecords

        collection.insert(databaseRecord).onComplete {
          case Failure(e) => Logger.error("Error saving to database", e)
          case Success(e) => Logger.info("Saved to database") 
        }

        externalJson
      } recover {
        case t: TimeoutException => Logger.error("Request to WS timed out", t); JsNull
      }
    }

    // Retrieves a response from the external API. If it is in cache, it will 
    // use that instead, otherwise it will call the webservice and then save
    // the response to the database.
    // Verification: Checks if the response is valid, if it isn't then it
    // won't be saved to the database. 
    // TODO: Either
    def get(): Future[JsValue] = {
      // Check if already saved to cache and if not then go get it
      val json: Future[JsValue] = checkCache flatMap {
        case None => getResponseAndCache
        case Some(x) => Future.successful(x)
      }

      json// TODO: .recover{}
    }

  }

}