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
import populator.instagram.entities.Media
import play.api.Play.current
import scala.concurrent.Future
import reactivemongo.api.indexes.{Index, IndexType}

object Base {
  def toEncoreFormat(m: Media): JsObject = {
    Json.obj(
      "caption" -> m.caption,
      "image_url" -> m.image_url,
      "user_name" -> m.user_name,
      "user_profile_picture" -> m.user_profile_picture,
      "type" -> "photo",
      "link" -> m.link
    )
  }

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("instagrams")

  collection.indexesManager.ensure(Index(
    key = Seq("link" -> IndexType.Ascending, "event_id" -> IndexType.Ascending),
    unique = true
  ))

  def insert(eventId: Long, instagram: Media): Unit = {
    val json = toEncoreFormat(instagram) ++ Json.obj("event_id" -> eventId)

    collection.insert(json)
  }

  def findAll(eventId: Long): Future[Seq[JsValue]] = {
    val query = Json.obj("event_id" -> eventId)

    collection.find(query).cursor[JsObject].collect[Seq]().map(_.map { j =>
      j ++ Json.obj("id" -> j \ "_id" \ "$oid")
    })
  }
}