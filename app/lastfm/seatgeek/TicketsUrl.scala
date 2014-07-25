package seatgeek

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api._
import play.modules.reactivemongo.json.collection.JSONCollection
import utils.ExternalApiCache
import com.github.nscala_time.time.Imports._
import com.netaporter.uri._
import com.netaporter.uri.dsl._
import lastfm.entities.Event
import scala.concurrent.Future

object TicketsUrl extends ExternalApiCache {

  def collection = db.collection[JSONCollection]("cache_seatgeek_url")
  def expiry = 3.days

  val key = Play.current.configuration.getString("seatgeek.aid").get

  // retrieved from https://github.com/backchatio/scala-inflector/blob/master/src/main/scala/Inflector.scala
  private def dasherize(word: String): String = {
    val spacesPattern = "[-\\s]".r
    val firstPattern = "([A-Z]+)([A-Z][a-z])".r
    val secondPattern = "([a-z\\d])([A-Z])".r
    val replacementPattern = "$1_$2"
    spacesPattern.replaceAllIn(
      secondPattern.replaceAllIn(
        firstPattern.replaceAllIn(
          word, replacementPattern), replacementPattern), "_").toLowerCase.replace('_', '-')
  }

  def get(event: Event): Future[Option[String]] = {
    val url = "http://api.seatgeek.com/2/events" ? ("aid" -> key) & 
                ("taxonomies.name" -> "concert") & ("performers.slug" -> dasherize(event.headliner)) &
                ("datetime_local" -> event.justDate)
    
    ExternalApiCall.get[JsValue](url).map { json =>
      ((json \ "events")(0) \ "url").asOpt[String]
    }
  }

}