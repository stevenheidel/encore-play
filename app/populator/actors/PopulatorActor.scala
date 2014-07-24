package populator.actors

import play.api._
import akka.actor._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import lastfm.actions.SingleEvent
import scala.collection.mutable.Set
import play.api.Play.current
import populator.algorithms._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api._
import scala.concurrent.Future
import play.api.libs.json._
import akka.pattern.pipe

sealed trait PopMessage
case class Start(eventId: Long) extends PopMessage
case class Check(eventId: Long) extends PopMessage

class PopulatorActor extends Actor {
  
  def receive = {
    case Start(eventId) => {
      addToSet(eventId)

      SingleEvent.get(eventId).map { e =>
        val fp = FlickrPopulator.populate(e)
        val ip = InstagramPopulator.populate(e)
        val yp = YoutubePopulator.populate(e)

        Future.sequence(Seq(fp, ip, yp)).onComplete {
          case Success(Seq(f, i, y)) => {
            removeFromSet(eventId)
            //Logger.debug(s"Found $f flickrs, $i instagrams, and $y youtubes")
          }
          case Failure(e) => removeFromSet(eventId); throw e
        }
      }
    }

    case Check(eventId) => {
      val stillWorking = checkInSet(eventId)

      stillWorking pipeTo sender
    }

  }

  def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("currently_populating")

  def addToSet(eventId: Long): Unit = {
    val query = Json.obj()
    val update = Json.obj(
      "$addToSet" -> Json.obj(
        "events" -> eventId
      )
    )

    collection.update(query, update, upsert = true)
  }

  def removeFromSet(eventId: Long): Unit = {
    val query = Json.obj()
    val update = Json.obj(
      "$pull" -> Json.obj(
        "events" -> eventId
      )
    )

    collection.update(query, update)
  }

  def checkInSet(eventId: Long): Future[Boolean] = {
    val query = Json.obj(
      "events" -> eventId
    )

    collection.find(query).one[JsValue].map(_.isDefined)
  }
}