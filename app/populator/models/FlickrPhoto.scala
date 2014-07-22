package populator.models

import populator.flickr.Photo
import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current

object FlickrPhoto extends Base {
  type Model = Photo

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("flickrs")

  def link(p: Photo): String = p.image_url

  def toDatabaseFormat(p: Photo): JsObject = {
    Json.obj(
      "caption" -> p.title,
      "user_name" -> p.user_name
    )
  }

  def toEncoreFormat(j: JsObject): JsObject = {
    j ++ Json.obj(
      "user_profile_picture" -> "http://on.encore.fm/assets/images/applogo.png",
      "image_url" -> j \ "link",
      "type" -> "photo"
    )
  }
}