package populator.models

import populator.youtube.Video
import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current

object YoutubeVideo extends Base {
  type Model = Video

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("youtubes")

  def link(v: Video): String = v.link

  def toDatabaseFormat(v: Video): JsObject = {
    Json.obj(
      "caption" -> v.caption,
      "image_url" -> v.image_url,
      "user_name" -> v.user_name
    )
  }

  def toEncoreFormat(j: JsObject): JsObject = {
    j ++ Json.obj(
      "user_profile_picture" -> "http://on.encore.fm/assets/images/applogo.png",
      "type" -> "video"
    )
  }
}