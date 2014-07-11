package populator.actors

import akka.actor._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import lastfm.actions.SingleEvent
import scala.collection.mutable.Set
import play.api.Play.current

sealed trait PopMessage
case class Start(eventId: Long) extends PopMessage
case class Check(eventId: Long) extends PopMessage
case class InstagramFinished(eventId: Long) extends PopMessage

class BasePopulator extends Actor {

  val instagramActor = Akka.system.actorOf(Props[InstagramPopulator])

  var instagramSet: Set[Long] = Set()

  def receive = {
    case Start(eventId) => {
      instagramSet += eventId

      SingleEvent.get(eventId).map { e =>
        instagramActor ! InstagramStart(e)
      }
    }

    case Check(eventId) => {
      val finished: Boolean = instagramSet.contains(eventId)

      sender ! finished
    }

    case InstagramFinished(eventId) => {
      instagramSet -= eventId
    }
  }
}