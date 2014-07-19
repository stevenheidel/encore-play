package populator.models

import populator.instagram.entities.Media
import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current

object InstagramPhoto extends Base {
  type Model = Media

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("instagrams")

  def link(m: Media): String = m.link

  def toDatabaseFormat(m: Media): JsObject = {
    Json.obj(
      "caption" -> m.caption,
      "image_url" -> m.image_url,
      "user_name" -> m.user_name,
      "user_profile_picture" -> m.user_profile_picture
    )
  }

  def toEncoreFormat(j: JsObject): JsObject = {
    j ++ Json.obj(
      "type" -> "photo"
    )
  }
}