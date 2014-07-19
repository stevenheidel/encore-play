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

sealed trait PopMessage
case class Start(eventId: Long) extends PopMessage
case class Check(eventId: Long) extends PopMessage

class PopulatorActor extends Actor {
  var workingSet: Set[Long] = Set()

  def receive = {
    case Start(eventId) => {
      SingleEvent.get(eventId).map { e =>
        val fp = FlickrPopulator.populate(e)
        val ip = InstagramPopulator.populate(e)
        val yp = YoutubePopulator.populate(e)

        Future.sequence(Seq(fp, ip, yp)).onComplete {
          case Success(Seq(f, i, y)) => {
            workingSet -= eventId
            Logger.debug(s"Found $f flickrs, $i instagrams, and $y youtubes")
          }
          case Failure(e) => workingSet -= eventId; throw e
        }
      }
    }

    case Check(eventId) => {
      val stillWorking: Boolean = workingSet.contains(eventId)

      sender ! stillWorking
    }

  }
}