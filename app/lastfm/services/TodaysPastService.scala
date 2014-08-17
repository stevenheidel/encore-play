package lastfm.services

import play.api._
import play.api.libs.json._
import lastfm.entities.Event
import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import utils.GeoPoint
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.indexes.{Index, IndexType}
import scala.concurrent.Future

/* This saves events from the current day for use in past events */

object TodaysPastService {
  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("service_todays_past")

  // Only get the first response for each day in each location
  collection.indexesManager.ensure(Index(
    key = Seq("date" -> IndexType.Ascending, "lat" -> IndexType.Ascending, "long" -> IndexType.Ascending),
    unique = true
  ))

  def getPast(latitude: Double, longitude: Double, date: String): Future[JsValue] = {
    val todaysDate = date.take("yyyy-MM-dd".length)
    val (lat, long) = GeoPoint.round(latitude, longitude, 1)

    // Get all past events, take most recent
    val query = Json.obj("date" -> Json.obj("$lt" -> todaysDate), "lat" -> lat, "long" -> long)
    val sort = Json.obj("date" -> -1)

    collection.find(query).sort(sort).one[JsObject].map {
      case Some(json) => {
        // Delete anything older
        deleteOlder(json)

        // Return the events
        (json \ "events")
      }
      case None => JsArray()
    }
  }

  // Remove from the databases any responses older than the one found
  def deleteOlder(json: JsObject): Unit = {
    val pastDate = (json \ "date")
    val lat = (json \ "lat").as[Double]
    val long = (json \ "long").as[Double]

    val query = Json.obj("date" -> Json.obj("$lt" -> pastDate), "lat" -> lat, "long" -> long)

    collection.remove(query)
  }

  def putToday(events: Seq[Event], latitude: Double, longitude: Double, date: String): Unit = {
    // 1 digit of precision allows for 11km of accuracy
    val (lat, long) = GeoPoint.round(latitude, longitude, 1)

    val record = Json.obj(
      "lat" -> lat,
      "long" -> long,
      "date" -> date.take("yyyy-MM-dd".length),
      "events" -> Json.toJson(events)
    )

    collection.insert(record)
  }
}