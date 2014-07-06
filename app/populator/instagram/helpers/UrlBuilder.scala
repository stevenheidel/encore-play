package populator.instagram.helpers

import com.netaporter.uri._
import com.netaporter.uri.dsl._
import play.api._

object UrlBuilder {

  val apiKey = Play.current.configuration.getString("instagram.client_id").get
  val apiVersion = "v1"

  val baseUrl = "https://api.instagram.com" / apiVersion
  val baseParams = Seq("client_id" -> apiKey)

  def location_search(latitude: Double, longitude: Double): Uri = {
    val allParams = baseParams ++ Seq("lat" -> latitude, "lng" -> longitude)

    (baseUrl / "locations/search").addParams(allParams)
  }

}