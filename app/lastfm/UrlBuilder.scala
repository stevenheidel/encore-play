package lastfm

import play.api.Play

import com.netaporter.uri.dsl._

object UrlBuilder {

  val apiKey = Play.current.configuration.getString("lastfm.key").get
  val apiVersion = "2.0"
  val apiFormat = "json"

  val baseUrl = "http://ws.audioscrobbler.com" / apiVersion
  val baseParams = ("format" -> apiFormat) :: ("api_key" -> apiKey) :: Nil

  def event_getInfo(eventId: Long): String = {
    constructUrl("event.getInfo", ("event" -> eventId) :: Nil)
  }

  private def constructUrl(method: String, params: List[Tuple2[String, Any]]) = {
    val allParams = ("method" -> method) :: params ::: baseParams
    baseUrl.addParams(allParams).toString
  }

}