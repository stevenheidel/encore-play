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

  // How long the cache is valid
  def expiry: Period

  // Take a response and turn it to JSON
  val jsonConverter: WSResponse => JsValue = _.json

  // How long to wait for External API
  val timeout = 10.seconds

  // Add expiry index and search index
  collection.indexesManager.ensure(Index(
    key = Seq("expiresAt" -> IndexType.Ascending),
    options = BSONDocument("expireAfterSeconds" -> 0)
  ))
  collection.indexesManager.ensure(Index(
    key = Seq("url" -> IndexType.Ascending)
  ))

  object ExternalApiCall {
    // Retrieves a response from the external API. If it is in cache, it will 
    // use that instead, otherwise it will call the webservice and then save
    // the response to the database.
    // Verification: Checks if the response is valid, if it isn't then it
    // won't be saved to the database. 
    def get[T](url: Uri)(implicit reads: Reads[T]): Future[T] = {
      // Did we send a request to the API?
      var calledApi = false

      val json: Future[JsValue] = checkCache(url).flatMap {
        case None => {
          calledApi = true
          getResponse(url)
        }
        case Some(x) => Future.successful(x)
      }

      // Attempt to validate the response
      json.map { j =>
        val obj = j.as[T]

        if (calledApi) saveToCache(url, j)

        obj
      }
    }
 
    // Send multiple parallel requests, then recombine after
    def getPar[T](urls: Seq[Uri], recombine: (T, T) => T)(implicit reads: Reads[T]): Future[T] = {
      val objs = urls.map(get[T])
      Future.sequence(objs).map(_.reduce(recombine))
    }

    // Check to see if the response is in the database
    private def checkCache(url: Uri): Future[Option[JsValue]] = {
      val query = Json.obj("url" -> url.toString())

      // Get only the first matching document and uncompress it
      collection.find(query).one[JsValue].map(_.map { x =>
        val cString = (x \ "response").toString()
        val uString = StringCompression.uncompress(cString)
        Json.parse(uString)
      })
    }

    private def getResponse(url: Uri): Future[JsValue] = {
      val response = WS.url(url.toString()).withRequestTimeout(timeout.millis.toInt).get()

      response.map(r => r.status match {
        case 200 =>
        case x => Logger.error(s"Call to $url returned status code $x")
      })

      // Parse the response as JSON
      response.map(jsonConverter)
    }

    private def saveToCache(url: Uri, response: JsValue): Unit = {
      val document = Json.obj(
        "url" -> url.toString(),
        "response" -> StringCompression.compress(response.toString()),
        "expiresAt" -> Json.obj(
          "$date" -> (DateTime.now + expiry).getMillis
        )
      )

      collection.insert(document) onFailure {
        case e => Logger.error("Could not save to database", e)
      }
    }
  }
}