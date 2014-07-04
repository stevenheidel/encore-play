package users

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import scala.language.implicitConversions
import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import reactivemongo.api.indexes.{Index, IndexType}
import play.api.Play.current

case class JsonUser(
  name: String,
  facebook_id: Long,
  oauth: Option[String],
  expiration_date: Option[String],
  invite_sent: Boolean
) {
  val facebook_image_url = s"https://graph.facebook.com/$facebook_id/picture?type=large"
  val isInviteSent: Boolean = invite_sent || oauth.isDefined
}

object JsonUser {
  // Reading from request
  implicit val userReads: Reads[JsonUser] = (
    (__ \ "name").read[String] ~
    ((__ \ "facebook_id").read[Long] orElse (__ \ "facebook_id").read[String].map(_.toLong)) ~
    (__ \ "oauth").readNullable[String] ~
    (__ \ "expiration_date").readNullable[String] ~
    (__ \ "invite_sent").readNullable[Boolean].map(_.getOrElse(false))
  )(JsonUser.apply _)

  implicit val userWrites: Writes[JsonUser] = new Writes[JsonUser] {
    def writes(u: JsonUser) = {
      Json.obj(
        "name" -> u.name,
        "facebook_image_url" -> u.facebook_image_url,
        "facebook_id" -> u.facebook_id.toString,
        "invite_sent" -> u.isInviteSent
      )
    }
  }

  implicit def databaseToJson(u: User): JsonUser = JsonUser(
    u.name, u.facebook_id, u.oauth, u.expiration_date, u.invite_sent
  )
}

case class User(
  name: String,
  facebook_id: Long,
  oauth: Option[String],
  expiration_date: Option[String],
  email: Option[String],
  invite_sent: Boolean,
  invite_timestamp: Option[DateTime],
  events: Seq[Long]
)

object User {
  // Only use for database reads and writes
  private implicit val userFormat: Format[User] = Json.format[User]

  // Convert from a request JSON to the database version
  implicit def jsonToDatabase(j: JsonUser): User = User(
    j.name, j.facebook_id, j.oauth, j.expiration_date, None, j.invite_sent, None, Seq()
  )

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")

  // Ensure only 1 user per facebook id
  collection.indexesManager.ensure(Index(
    key = Seq("facebook_id" -> IndexType.Ascending),
    unique = true
  ))

  def insert(user: User): Future[User] = {
    collection.insert(user).flatMap { lastError =>
      get(user.facebook_id).map(_.get)
    }
  }

  def update(user: User): Future[User] = {
    collection.update(user.facebook_id, user).flatMap { lastError =>
      get(user.facebook_id).map(_.get)
    }
  }

  def upsert(user: User): Future[User] = {
    collection.update(user.facebook_id, user, upsert = true).flatMap { lastError =>
      get(user.facebook_id).map(_.get)
    }
  }

  def get(facebook_id: Long): Future[Option[User]] = {
    val query = Json.obj("facebook_id" -> facebook_id)

    collection.find(query).one[User]
  }

  def hasEvent(facebook_id: Long, event_id: Long): Future[Boolean] = {
    val query = Json.obj(
      "facebook_id" -> facebook_id,
      "events" -> event_id
    )

    collection.find(query).one[User].map(_.isDefined)
  }

  def addEvent(facebook_id: Long, event_id: Long): Future[User] = {
    val query = Json.obj("facebook_id" -> facebook_id)
    val update = Json.obj(
      "$addToSet" -> Json.obj(
        "events" -> event_id
      )
    )

    collection.update(query, update).flatMap { lastError =>
      get(facebook_id).map(_.get)
    }
  }

  def removeEvent(facebook_id: Long, event_id: Long): Future[User] = {
    val query = Json.obj("facebook_id" -> facebook_id)
    val update = Json.obj(
      "$pull" -> Json.obj(
        "events" -> event_id
      )
    )

    collection.update(query, update).flatMap { lastError =>
      get(facebook_id).map(_.get)
    }
  }
}