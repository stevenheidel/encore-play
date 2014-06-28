package lastfm

import com.netaporter.uri._
import com.netaporter.uri.dsl._
import play.api._

case class Pagination(limit: Int = 50, page: Int = 1)

object UrlBuilder {

  val apiKey = Play.current.configuration.getString("lastfm.key").get
  val apiVersion = "2.0"
  val apiFormat = "json"

  val baseUrl = "http://ws.audioscrobbler.com" / apiVersion
  val baseParams = Seq("format" -> apiFormat, "api_key" -> apiKey)

  // http://www.last.fm/api/show/artist.getEvents
  def artist_getEvents(artistName: String, pagination: Pagination): Uri = {
    constructUrl("artist.getEvents", Seq("artist" -> artistName), Some(pagination))
  }

  // http://www.last.fm/api/show/artist.getInfo
  def artist_getInfo(artistName: String): Uri = {
    constructUrl("artist.getInfo", Seq("artist" -> artistName))
  }

  // http://www.last.fm/api/show/artist.getPastEvents
  def artist_getPastEvents(artistName: String, pagination: Pagination): Uri = {
    constructUrl("artist.getPastEvents", Seq("artist" -> artistName), Some(pagination))
  }

  // http://www.last.fm/api/show/artist.search
  def artist_search(term: String): Uri = {
    constructUrl("artist.search", Seq("artist" -> term))
  }

  // http://www.last.fm/api/show/event.getInfo
  def event_getInfo(eventId: Long): Uri = {
    constructUrl("event.getInfo", Seq("event" -> eventId))
  }

  // http://www.last.fm/api/show/geo.getEvents
  def geo_getEvents(lat: Double, long: Double, distance: Double, pagination: Pagination): Uri = {
    constructUrl("geo.getEvents", Seq("lat" -> lat, "long" -> long, "distance" -> distance), Some(pagination))
  }

  private def constructUrl(
      method: String, 
      params: Seq[Tuple2[String, Any]], 
      pagination: Option[Pagination] = None): Uri = {
    val allParams = baseParams ++ params ++ Seq("method" -> method) ++
                      pagination.map("page" -> _.page) ++ pagination.map("limit" -> _.limit)
    baseUrl.addParams(allParams)
  }

}