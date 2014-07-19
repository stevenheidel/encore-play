package populator.actors

import play.api._
import lastfm.entities.{Event, Venue}
import populator.instagram.entities.{Location, Media}
import populator.instagram.actions._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.StringSimilarity
import scala.concurrent.Future
import populator.foursquare.SearchVenues
import com.github.nscala_time.time.Imports._
import akka.actor._
import play.api.libs.concurrent.Akka
import scala.util.{Success, Failure}
import play.api.Play.current

case class InstagramStart(event: Event)

class InstagramPopulator extends Actor {
  def receive = {
    case InstagramStart(event: Event) => {
      val f = populate(event)

      f.onComplete {
        case Success(_) => sender ! InstagramFinished(event.id)
        case Failure(e) => sender ! InstagramFinished(event.id); throw e
      }
    }
  }

  def populate(event: Event): Future[Boolean] = {
    val locations = instagramLocationsForVenue(event.venue.get)

    locations.map { ls =>
      // Populate each location with images
      ls.map(recurse(event))

      true
    }
  }

  def recurse(event: Event, maxId: Option[String] = None)(location: Location): Unit = {
    val f = for {
      start <- event.utcStartTime
      end = start + 6.hours // Arbitrarily say event lasted 6 hours
      response <- LocationRecentMedia.get(location, start, end, maxId)
    } yield {
      response.media.map { m =>
        // Save to database
        populator.models.InstagramPhoto.insert(event.id, m)
      }

      // If there are more responses to get, then go again with next max id
      val nextMaxId = response.pagination.next_max_id
      if (nextMaxId.isDefined) {
        recurse(event, nextMaxId)(location)
      }
    }

    // Make sure errors don't dissapear into the abyss
    f onFailure {
      case x => throw x
    }
  }

  def filterLocations(venueName: String)(locations: Seq[Location]): Seq[Location] = {
    val similarityThreshold = 0.25

    locations.filter(l => StringSimilarity.compareStrings(l.name, venueName) > similarityThreshold)
  }

  def instagramLocationsForVenue(venue: Venue): Future[Seq[Location]] = {
    // First get all the ones from Foursquare
    val foursquareLocations = SearchVenues.get(venue.name, venue.lat.get, venue.long.get).flatMap { fs =>
      // and correlate with Instagram locations
      val is = fs.map(LocationSearch.get(_))
      // this line makes sure to only get the foursquare location if it's found by Instagram
      Future.sequence(is).map(_.map(_.headOption).flatten)
    }

    // Then do an Instagram search on venue latitude and longitude
    val instagramLocations = LocationSearch.get(venue.lat.get, venue.long.get).map(filterLocations(venue.name))

    // Then go through each found location and search around it as well
    val bonusLocations = for {
      fs <- foursquareLocations
      is <- instagramLocations
    } yield {
      val bs = (fs ++ is).map { l =>
        LocationSearch.get(l.latitude, l.longitude).map(filterLocations(venue.name))
      }

      Future.sequence(bs).map(_.flatten)
    }

    // Return all the distinct results
    for {
      fs <- foursquareLocations
      is <- instagramLocations
      bs <- bonusLocations.flatMap(identity)
    } yield {
      (fs ++ is ++ bs).distinct
    }
  }
}