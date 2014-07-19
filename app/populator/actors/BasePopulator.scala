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
case class FlickrFinished(eventId: Long) extends PopMessage
case class InstagramFinished(eventId: Long) extends PopMessage
case class YoutubeFinished(eventId: Long) extends PopMessage

class BasePopulator extends Actor {
  val flickrActor = Akka.system.actorOf(Props[FlickrPopulator])
  val instagramActor = Akka.system.actorOf(Props[InstagramPopulator])
  val youtubeActor = Akka.system.actorOf(Props[YoutubePopulator])

  var flickrSet: Set[Long] = Set()
  var instagramSet: Set[Long] = Set()
  var youtubeSet: Set[Long] = Set()

  def receive = {
    case Start(eventId) => {
      flickrSet += eventId
      instagramSet += eventId
      youtubeSet += eventId

      SingleEvent.get(eventId).map { e =>
        flickrActor ! FlickrStart(e)
        instagramActor ! InstagramStart(e)
        youtubeActor ! YoutubeStart(e)
      }
    }

    case Check(eventId) => {
      val stillWorking: Boolean = flickrSet.contains(eventId) ||
                                  instagramSet.contains(eventId) ||
                                  youtubeSet.contains(eventId)

      sender ! stillWorking
    }

    case FlickrFinished(eventId) => {
      flickrSet -= eventId
    }

    case InstagramFinished(eventId) => {
      instagramSet -= eventId
    }

    case YoutubeFinished(eventId) => {
      youtubeSet -= eventId
    }
  }
}