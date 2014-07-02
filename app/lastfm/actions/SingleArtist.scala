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
import lastfm.responses.SingleResponse

object SingleArtist extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("single_artists")
  def expiry = 1.day

  def get(artistName: String): Future[Artist] = {
    ExternalApiCall.get[SingleResponse](UrlBuilder.artist_getInfo(artistName)).map { r =>
      r.artist.get
    }
  }

}