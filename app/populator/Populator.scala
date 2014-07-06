package populator

import lastfm.entities.Venue
import populator.instagram.entities.Location
import populator.instagram.actions._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.StringSimilarity
import scala.concurrent.Future
import populator.foursquare.SearchVenues

object Populator {
  def filterLocations(venueName: String)(locations: Seq[Location]) : Seq[Location] = {
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