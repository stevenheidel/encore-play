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

  def collection = db.collection[JSONCollection]("ArtistSearch")
  def expiry = 1.day

  def get(term: String): Future[Seq[Artist]] = {
    val path = UrlBuilder.artist_search(term)
    val indexParameters = Json.obj("term" -> term)
    val searchParameters = indexParameters

    val response = ExternalApiCall(path, indexParameters, searchParameters)
    
    response.get().map(json => json.as[SearchResult].artistMatches)
  }

}