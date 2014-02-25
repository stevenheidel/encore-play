package lastfm

import play.api.Play

object UrlBuilder {
  val apiKey = Play.current.configuration.getString("lastfm.key").get
  val baseUrl = "http://ws.audioscrobbler.com/2.0/"

  def event_getInfo(eventId: Long): String = {
    baseUrl + "?method=event.getInfo&event=" + eventId.toString + "&api_key=" + apiKey + "&format=json"
  }
}