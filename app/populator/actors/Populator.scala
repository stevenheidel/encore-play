package populator.actors

import akka.actor._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import lastfm.actions.SingleEvent
import populator.algorithms._
import scala.collection.mutable.Set
import scala.util.{Success, Failure}

sealed trait PopMessage
case class Start(eventId: Long) extends PopMessage
case class Check(eventId: Long) extends PopMessage

class Populator extends Actor {

  var instagram: Set[Long] = Set()

  def receive = {
    case Start(eventId) => {
      instagram += eventId

      SingleEvent.get(eventId).map(Instagrams.populate) onComplete {
        case Success(_) => instagram -= eventId
        case Failure(e) => instagram -= eventId; throw e
      }
    }

    case Check(eventId) => {
      sender ! instagram.contains(eventId)
    }
  }
}