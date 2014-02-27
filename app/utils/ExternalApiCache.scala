package utils

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.{Success, Failure}

import reactivemongo.api._
import play.modules.reactivemongo.json.collection.JSONCollection

trait ExternalApiCache {

  // The MongoDB collection to use for the current method / group of methods
  def collection: JSONCollection

  /*
  val name (of the service)
  val expiry
  def verify
  */

  class ExternalApiCall(
    path: String,
    searchParameters: JsValue,
    indexParameters: JsValue
  ) {

    // Check to see if the response is in the database
    private def checkCache: Future[Option[JsValue]] = {
      val dbCursor: Cursor[JsValue] = collection.find(searchParameters).cursor[JsValue]
      val dbList: Future[List[JsValue]] = dbCursor.collect[List]()

      dbList.map {
        case Nil => None
        // TODO: Get the response itself, not the whole document
        case x :: xs => Some(x)
      }
    }

    // Get the response from external API and then cache it to the database
    private def getResponseAndCache: Future[JsValue] = {
      Logger.info("Called External API")
      
      WS.url(path).get().map { externalResponse =>
        val externalJson = externalResponse.json

        // TODO: Verify response here

        // TODO: Save with index parameters
        collection.insert(externalJson).onComplete {
          case Failure(e) => Logger.error("Error saving to database", e)
          case Success(e) => Logger.info("Saved to database: " + e) 
        }

        externalJson
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