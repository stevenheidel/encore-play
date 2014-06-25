package lastfm.actions

import lastfm.UrlBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import lastfm.entities.Artist
import scala.concurrent.Future
import scala.util.{Try, Success, Failure}

object SingleArtist extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("single_artists")
  def expiry = 1.minute

  def get(artistName: String): Future[Try[Artist]] = {
    val path = UrlBuilder.artist_getInfo(artistName)
    val searchParameters = Json.obj("artist_name" -> artistName)
    val indexParameters = searchParameters

    val response = ExternalApiCall(path, searchParameters, indexParameters)
    
    response.get().map(json => Try((json \ "artist").as[Artist]))
  }

}