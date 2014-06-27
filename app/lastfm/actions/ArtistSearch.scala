package lastfm.actions

import lastfm.UrlBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import lastfm.entities.Artist
import lastfm.responses.SearchResult
import scala.concurrent.Future

object ArtistSearch extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("artist_search")
  def expiry = 1.minute

  def get(term: String): Future[Seq[Artist]] = {
    val path = UrlBuilder.artist_search(term)
    val searchParameters = Json.obj("term" -> term)
    val indexParameters = searchParameters

    val response = ExternalApiCall(path, searchParameters, indexParameters)
    
    response.get().map(json => json.as[SearchResult].artistMatches)
  }

}