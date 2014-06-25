package lastfm

import com.netaporter.uri.dsl._
import play.api.Play

object UrlBuilder {

  val apiKey = Play.current.configuration.getString("lastfm.key").get
  val apiVersion = "2.0"
  val apiFormat = "json"

  val baseUrl = "http://ws.audioscrobbler.com" / apiVersion
  val baseParams = List("format" -> apiFormat, "api_key" -> apiKey)

  // http://www.last.fm/api/show/artist.getInfo
  def artist_getInfo(artistName: String): String = {
    constructUrl("artist.getInfo", List("artist" -> artistName))
  }

  // http://www.last.fm/api/show/artist.getPastEvents
  def artist_getPastEvents(artistName: String, page: Int = 1, limit: Int = 50): String = {
    constructUrl("artist.getPastEvents", List("artist" -> artistName, "page" -> page, "limit" -> limit))
  }

  // http://www.last.fm/api/show/event.getInfo
  def event_getInfo(eventId: Long): String = {
    constructUrl("event.getInfo", List("event" -> eventId))
  }

  private def constructUrl(method: String, params: List[Tuple2[String, Any]]) = {
    val allParams = ("method" -> method) :: params ::: baseParams
    baseUrl.addParams(allParams).toString()
  }

}