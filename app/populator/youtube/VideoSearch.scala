package populator.youtube

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import scala.concurrent.Future
import play.api._
import com.netaporter.uri._
import com.netaporter.uri.dsl._

object VideoSearch extends ExternalApiCache {
  def collection = db.collection[JSONCollection]("cache_youtube_video_search")
  def expiry = 1.hour

  val key = Play.current.configuration.getString("google.key").get

  def get(eventName: String, city: String, date: DateTime): Future[Seq[Video]] = {
    val writeFormat = DateTimeFormat.forPattern("MMMM d yyyy").withLocale(java.util.Locale.ENGLISH)
    val formattedDate = writeFormat.print(date)

    val url = "https://www.googleapis.com/youtube/v3/search" ? ("key" -> key) &
                ("part" -> "snippet") & ("type" -> "video") & ("videoEmbeddable" -> true) &
                ("maxResults" -> 20) & ("q" -> s"$eventName $city $formattedDate")

    ExternalApiCall.get[JsValue](url).flatMap { json =>
      val ids = (json \ "items" \\ "id").map(id => (id \ "videoId").as[String])
      
      // Use Youtube API v2 to retrieve streaming details
      // WARNING: This API expires on March 14, 2015
      val urls = ids.map { id =>
        ("https://gdata.youtube.com/feeds/api/videos" / id) ? ("v" -> 2) & ("alt" -> "json") & ("key" -> key)
      }

      ExternalApiCall.getPar[Video](urls)
    }
  }
}