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
    val user = request.body.as[User]

    collection.insert(user).map { lastError =>
      Ok(Json.obj(
        "user" -> user.jsonShort
      ))
    }
  }

  def update(facebook_id: Long) = Action.async(parse.json) { request =>
    val user = request.body.as[User]

    val query = Json.obj("facebook_id" -> user.facebook_id)

    collection.update(query, user).map { lastError =>
      Ok(Json.obj(
        "user" -> user.jsonShort
      ))
    }
  }

  // Branches based on whether there is a lastfm_id parameter in the body or not
  def listOrCheck(facebook_id: Long) = Action.async(parse.json) { request =>
    if (request.body.toString.isEmpty) {
      listEvents(facebook_id)
    } else {
      val lastfm_id = request.body \ "lastfm_id"
      checkEvent(facebook_id, lastfm_id.toString.toLong)
    }
  }

  def listEvents(facebook_id: Long) = {
    val query = Json.obj("facebook_id" -> facebook_id)

    collection.find(query).one[JsValue].flatMap { x =>
      val user = x.get
      val eventIds = (user \ "events").as[Seq[Long]]
      val eventsF = lastfm.actions.SingleEvent.multi(eventIds)

      eventsF.map { events =>
        Ok(Json.obj(
          "events" -> Json.obj(
            "past" -> Json.parse("[]"),
            "future" -> Json.toJson(events)
          )
        ))
      }
    }
  }

  def checkEvent(facebook_id: Long, event_id: Long) = {
    val query = Json.obj(
      "facebook_id" -> facebook_id,
      "events" -> event_id
    )

    collection.find(query).one[JsValue].map { x =>
      Ok(Json.obj(
        "response" -> x.isDefined
      ))
    }
  }

  def addEvent(facebook_id: Long) = Action.async(parse.json) { request =>
    val lastfm_id = request.body \ "lastfm_id"

    val query = Json.obj("facebook_id" -> facebook_id)
    val update = Json.obj(
      "$addToSet" -> Json.obj(
        "events" -> lastfm_id.toString.toLong
      )
    )

    collection.update(query, update).map { lastError =>
      Ok(Json.obj("response" -> "success"))
    }
  }

  def removeEvent(facebook_id: Long, event_id: Long) = Action.async { 
    val query = Json.obj("facebook_id" -> facebook_id)
    val update = Json.obj(
      "$pull" -> Json.obj(
        "events" -> event_id
      )
    )

    collection.update(query, update).map { lastError =>
      Ok(Json.obj("response" -> "success"))
    }
  }

  // UNIMPLEMENTED
  def addFriends(facebook_id: Long, event_id: Long) = Action {
    Ok(Json.parse("[]"))
  }

  // UNIMPLEMENTED
  def listFriends(facebook_id: Long, event_id: Long) = Action {
    Ok(Json.parse("[]"))
  }
}