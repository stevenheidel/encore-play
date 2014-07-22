package populator.algorithms

import play.api._
import lastfm.entities.{Event, Venue}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import populator.flickr._
import com.github.nscala_time.time.Imports._

object FlickrPopulator {
  def populate(event: Event): Future[Int] = {
    event.utcStartTime.flatMap { time =>
      val tagged = PhotoSearch.get(event.tag)
      val vicinity = PhotoSearch.get(event.venue.get.lat.get, event.venue.get.long.get, time, time + 6.hours)

      for {
        photos1 <- tagged
        photos2 <- vicinity
      } yield {
        val photos = photos1 ++ photos2

        photos.map { p =>
          populator.models.FlickrPhoto.insert(event.id, p)
        }

        photos.length
      }
    }
  }
}