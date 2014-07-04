package users

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import reactivemongo.api._
import reactivemongo.core.commands.LastError
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import reactivemongo.api.indexes.{Index, IndexType}
import play.api.Play.current

case class UserFriends(
  facebook_id: Long,
  event_id: Long,
  friend_ids: Seq[Long]
)

object UserFriends {
  implicit val userFriendsFormat: Format[UserFriends] = Json.format[UserFriends]

  def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("user_friends")

  def update(facebook_id: Long, event_id: Long, friend_ids: Seq[Long]): Future[UserFriends] = {
    val userFriends = UserFriends(facebook_id, event_id, friend_ids)

    collection.insert(userFriends).map { lastError =>
      userFriends
    }
  }

  def get(facebook_id: Long, event_id: Long): Future[UserFriends] = {
    val query = Json.obj("facebook_id" -> facebook_id, "event_id" -> event_id)

    collection.find(query).one[UserFriends].map(_.get)
  }
}