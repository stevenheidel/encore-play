package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import models.User

object UsersController extends Controller with MongoController {
  def collection: JSONCollection = db.collection[JSONCollection]("users")

  // Ensure only 1 user per facebook id
  collection.indexesManager.ensure(Index(
    key = Seq("facebook_id" -> IndexType.Ascending),
    unique = true
  ))

  def create = Action.async(parse.json) { request =>
    request.body.validate[User].map { user =>
      collection.insert(user).map { lastError =>
        Ok(Json.obj(
          "user" -> Json.obj(
            "name" -> user.name,
            "facebook_image_url" -> user.facebook_image_url
          )
        ))
      }
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }
}