package populator.models

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current
import scala.concurrent.Future
import reactivemongo.api.indexes.{Index, IndexType}
import utils.GZipHelper

trait Base {
  type Model

  // Get the unique link, used for indexing purposes
  def link(m: Model): String
  // Take model object and save it to database
  def toDatabaseFormat(m: Model): JsObject
  // Take what's been saved in database and show it to the response
  def toEncoreFormat(j: JsObject): JsObject

  def collection: JSONCollection

  collection.indexesManager.ensure(Index(
    key = Seq("link" -> IndexType.Ascending, "event_id" -> IndexType.Ascending),
    unique = true
  ))

  def insert(eventId: Long, model: Model): Unit = {
    val json = Json.obj(
      "event_id" -> eventId,
      "link" -> link(model),
      "media" -> GZipHelper.deflate(toDatabaseFormat(model).toString)
    )

    collection.insert(json)
  }

  def findAll(eventId: Long): Future[Seq[JsValue]] = {
    val query = Json.obj("event_id" -> eventId)

    collection.find(query).cursor[JsObject].collect[Seq]().map(_.map { j =>
      val cString = (j \ "media").toString
      val uString = GZipHelper.inflate(cString)
      // Unzip and also add id as the unique database id
      toEncoreFormat(Json.parse(uString).as[JsObject]) ++ Json.obj(
        "id" -> j \ "_id" \ "$oid",
        "link" -> j \ "link"
      )
    })
  }
}