package users

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future
import lastfm.Lastfm
import users.JsonUser._

object UsersController extends Controller {

  // Takes the request turns it into JSON
  // TODO: This should be better, I assumed JSON to start with
  def convertFormUrlEncodedToJson(implicit request: Request[AnyContent]) = {
    Logger.debug(request.body.toString)
    val json = Json.toJson(request.body.asFormUrlEncoded.get.map {
      case (k,v) => (k, v.head)
    })
    Logger.debug(json.toString)
    json
  }
  
  def create = Action.async { implicit request =>
    val json = convertFormUrlEncodedToJson
    val jUser = json.as[User]

    User.insert(jUser).map { user =>
      Ok(Json.obj(
        "user" -> Json.toJson(user)
      ))
    }
  }

  def update(facebook_id: Long) = Action.async { implicit request =>
    val json = convertFormUrlEncodedToJson
    val jUser = json.as[User]

    User.update(jUser).map { user =>
      Ok(Json.obj(
        "user" -> Json.toJson(user)
      ))
    }
  }

  def checkEvent(facebook_id: Long, event_id: Long) = Action.async {
    User.hasEvent(facebook_id, event_id).map { x =>
      Ok(Json.obj(
        "response" -> x
      ))
    }
  }

  def listEvents(facebook_id: Long, date: String) = Action.async {
    User.getOpt(facebook_id).flatMap { user =>
      val eventIds = user.flatMap(_.events).getOrElse(Seq())
      val eventsF = Lastfm.getEvents(eventIds)

      eventsF.map { events =>
        val (past, future) = events.partition(_.isFuture(date))

        Ok(Json.obj(
          "events" -> Json.obj(
            "past" -> Json.toJson(past),
            "future" -> Json.toJson(future)
          )
        ))
      }
    }
  }

  def addEvent(facebook_id: Long) = Action.async { implicit request =>
    val json = convertFormUrlEncodedToJson
    val lastfm_id = (json \ "lastfm_id").as[String].toLong

    User.addEvent(facebook_id, lastfm_id).map { lastError =>
      Ok(Json.obj("response" -> "success"))
    }
  }

  def removeEvent(facebook_id: Long, event_id: Long) = Action.async { 
    User.removeEvent(facebook_id, event_id).map { lastError =>
      Ok(Json.obj("response" -> "success"))
    }
  }

  // FRIENDS

  def addFriends(facebook_id: Long, event_id: Long) = Action.async { implicit request =>
    val json = convertFormUrlEncodedToJson
    val rawFriends = (json \ "friends").as[Seq[User]]

    // Add all to database if not there already
    val friends = Future.sequence(rawFriends.map(User.upsert(_)))

    friends.flatMap { fs =>
      val friendIds = fs.map(_.facebook_id)

      UserFriends.update(facebook_id, event_id, friendIds).map { userFriends =>
        Ok(Json.toJson(fs))
      }
    }
  }

  def listFriends(facebook_id: Long, event_id: Long) = Action.async {
    UserFriends.get(facebook_id, event_id).flatMap { userFriends =>
      val friends = Future.sequence(userFriends.friend_ids.map(User.get(_)))

      friends.map { fs =>
        Ok(Json.toJson(fs))
      }
    }
  }
}