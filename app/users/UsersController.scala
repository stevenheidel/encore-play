package users

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future
import lastfm.Lastfm
import users.JsonUser._

object UsersController extends Controller {
  
  def create = Action.async(parse.json) { request =>
    val jUser = request.body.as[User]

    User.insert(jUser).map { user =>
      Ok(Json.obj(
        "user" -> Json.toJson(user)
      ))
    }
  }

  def update(facebook_id: Long) = Action.async(parse.json) { request =>
    val jUser = request.body.as[User]

    User.update(jUser).map { user =>
      Ok(Json.obj(
        "user" -> Json.toJson(user)
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
    User.get(facebook_id).flatMap { user =>
      val eventIds = user.events
      val eventsF = Lastfm.getEvents(eventIds)

      eventsF.map { events =>
        Ok(Json.obj(
          "events" -> Json.obj(
            "past" -> JsArray(),
            "future" -> Json.toJson(events)
          )
        ))
      }
    }
  }

  def checkEvent(facebook_id: Long, event_id: Long) = {
    User.hasEvent(facebook_id, event_id).map { x =>
      Ok(Json.obj(
        "response" -> x
      ))
    }
  }

  def addEvent(facebook_id: Long) = Action.async(parse.json) { request =>
    val lastfm_id = request.body \ "lastfm_id"

    User.addEvent(facebook_id, lastfm_id.toString.toLong).map { lastError =>
      Ok(Json.obj("response" -> "success"))
    }
  }

  def removeEvent(facebook_id: Long, event_id: Long) = Action.async { 
    User.removeEvent(facebook_id, event_id).map { lastError =>
      Ok(Json.obj("response" -> "success"))
    }
  }

  // FRIENDS

  def addFriends(facebook_id: Long, event_id: Long) = Action.async(parse.json) { request =>
    val rawFriends = (request.body \ "friends").as[Seq[User]]

    // Add all to database if not there already
    val friends = Future.sequence(rawFriends.map(User.upsert(_)))

    friends.flatMap { fs =>
      val friendIds = fs.map(_.facebook_id)

      UserFriends.update(facebook_id, event_id, friendIds).map { userFriends =>
        Ok(Json.toJson(fs.asInstanceOf[Seq[User]]))
      }
    }
  }

  def listFriends(facebook_id: Long, event_id: Long) = Action.async {
    UserFriends.get(facebook_id, event_id).map { userFriends =>
      val friends = userFriends.friend_ids.map(User.get(_))

      Ok(Json.toJson(friends.asInstanceOf[Seq[User]]))
    }
  }
}