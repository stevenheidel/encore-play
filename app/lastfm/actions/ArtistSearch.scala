package lastfm.actions

import lastfm.helpers.UrlBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import lastfm.entities.Artist
import lastfm.responses.SearchResult
import scala.concurrent.Future

object ArtistSearch extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_lastfm_artist_search")
  def expiry = 1.week

  def get(term: String): Future[Seq[Artist]] = {
    ExternalApiCall.get[SearchResult](UrlBuilder.artist_search(term)).map { r =>
      r.artistMatches
    }
  }

}