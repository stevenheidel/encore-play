package users

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import reactivemongo.core.commands.LastError
import scala.concurrent.Future
import reactivemongo.api.indexes.{Index, IndexType}
import play.api.Play.current

// Reading and writing from request
object JsonUser {
  implicit val userReads: Reads[User] = (
    (__ \ "name").readNullable[String] ~
    ((__ \ "facebook_id").read[Long] orElse (__ \ "facebook_id").read[String].map(_.toLong)) ~
    (__ \ "oauth").readNullable[String] ~
    (__ \ "expiration_date").readNullable[String] ~
    Reads.pure(None) ~
    (__ \ "invite_sent").readNullable[Boolean] ~
    (__ \ "invite_timestamp").readNullable[String] ~
    Reads.pure(None)
  )(User.apply _)

  implicit val userWrites: Writes[User] = new Writes[User] {
    def writes(u: User) = {
      Json.obj(
        "name" -> u.name,
        "facebook_image_url" -> u.facebook_image_url,
        "facebook_id" -> u.facebook_id.toString,
        "invite_sent" -> u.isInviteSent
      )
    }
  }
}

// The only thing in User we can know for certain is facebook_id,
// if they haven't been logged in properly
case class User(
  name: Option[String],
  facebook_id: Long,
  oauth: Option[String],
  expiration_date: Option[String],
  email: Option[String],
  invite_sent: Option[Boolean],
  invite_timestamp: Option[String],
  events: Option[Seq[Long]]
) {
  val facebook_image_url = s"https://graph.facebook.com/$facebook_id/picture?type=large"
  val isInviteSent: Boolean = invite_sent.getOrElse(false) || oauth.isDefined
}

object User {
  // Only use for database reads and writes
  private implicit val userFormat: Format[User] = Json.format[User]

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")

  // Ensure only 1 user per facebook id
  collection.indexesManager.ensure(Index(
    key = Seq("facebook_id" -> IndexType.Ascending),
    unique = true
  ))

  // Throw caution to the wind and get latest version of user from the database
  // Used for returning updated versions in other methods
  def get(user: User): Future[User] = get(user.facebook_id)  
  def get(facebook_id: Long): Future[User] = {
    getOpt(facebook_id).map(_.get)
  }

  def getOpt(user: User): Future[Option[User]] = getOpt(user.facebook_id)
  def getOpt(facebook_id: Long): Future[Option[User]] = {
    val query = Json.obj("facebook_id" -> facebook_id)

    collection.find(query).one[User]
  }

  def insert(user: User): Future[User] = {
    collection.insert(user).flatMap(_ => get(user)).recoverWith {
      // User already exists
      case LastError(_,_,Some(11000),_,_,_,_) => update(user)
    }
  }

  def update(user: User, upsert: Boolean = false): Future[User] = {
    val query = Json.obj("facebook_id" -> user.facebook_id)
    val fields = Json.obj("$set" -> Json.toJson(user))

    collection.update(query, fields, upsert = upsert).flatMap(_ => get(user))
  }

  def upsert(user: User): Future[User] = update(user, true)

  def hasEvent(facebook_id: Long, event_id: Long): Future[Boolean] = {
    val query = Json.obj(
      "facebook_id" -> facebook_id,
      "events" -> event_id
    )

    collection.find(query).one[User].map(_.isDefined)
  }

  def addEvent(facebook_id: Long, event_id: Long): Future[LastError] = {
    val query = Json.obj("facebook_id" -> facebook_id)
    val update = Json.obj(
      "$addToSet" -> Json.obj(
        "events" -> event_id
      )
    )

    collection.update(query, update, upsert = true)
  }

  def removeEvent(facebook_id: Long, event_id: Long): Future[LastError] = {
    val query = Json.obj("facebook_id" -> facebook_id)
    val update = Json.obj(
      "$pull" -> Json.obj(
        "events" -> event_id
      )
    )

    collection.update(query, update, upsert = true)
  }

  def all(): Future[Seq[User]] = {
    val query = Json.obj()

    collection.find(query).cursor[User].collect[Seq]()
  }
}