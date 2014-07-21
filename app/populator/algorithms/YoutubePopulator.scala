package populator.algorithms

import play.api._
import lastfm.entities.{Event, Venue}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import populator.youtube._
import com.github.nscala_time.time.Imports._

object YoutubePopulator {
  def populate(event: Event): Future[Int] = {
    val videos = VideoSearch.get(event.title, event.venue.get.city, event.localDate).map { vs =>
      for {
        v <- vs
        if isValid(event, v)
      } yield {
        // Insert into database
        populator.models.YoutubeVideo.insert(event.id, v)

        v
      }
    }

    videos.map(_.length)
  }

  // Ensure that it's a video of the event
  def isValid(event: Event, video: Video): Boolean = {
    val string = s"${event.title} - ${video.description}".toLowerCase

    val eventName = event.title.toLowerCase
    val headliner = event.headliner.toLowerCase
    val city = event.venue.get.city.toLowerCase
    val venueName = event.venue.get.name.toLowerCase
    val date = event.localDate

    // Make sure video contains event name / artist, city / venue, and the right date
    (string.contains(eventName) || string.contains(headliner)) && 
        (string.contains(city) || string.contains(venueName)) && 
        validDate(string, date)
  }

  def validDate(string: String, date: DateTime): Boolean = {
    // Returns a list of representations of the day, month, and year
    def getReps(char: String): Seq[String] = {
      (1 to 4).map(char * _).map { f =>
        val writeFormat = DateTimeFormat.forPattern(f).withLocale(java.util.Locale.ENGLISH)
        writeFormat.print(date)
      }
    }

    // Make sure all videos contain the day, month, and year in some format
    Seq("d", "M", "y").forall { char =>
      getReps(char).exists(string contains _)
    }
  }
}