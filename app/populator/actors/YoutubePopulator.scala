package populator.actors

import play.api._
import lastfm.entities.{Event, Venue}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import com.github.nscala_time.time.Imports._
import akka.actor._
import play.api.libs.concurrent.Akka
import scala.util.{Success, Failure}
import play.api.Play.current

case class YoutubeStart(event: Event)

class YoutubePopulator extends Actor {
  def receive = {
    case FlickrStart(event: Event) => {
      val f = populate(event)

      f.onComplete {
        case Success(_) => sender ! YoutubeFinished(event.id)
        case Failure(e) => sender ! YoutubeFinished(event.id); throw e
      }
    }
  }

  def populate(event: Event): Future[Boolean] = {
    Future { true }
  }
}