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
    val params = Seq("lat" -> latitude, "lng" -> longitude)

    constructUrl("locations/search", params)
  }

  def foursquare_location(foursquareId: String): Uri = {
    val params = Seq("foursquare_v2_id" -> foursquareId)

    constructUrl("locations/search", params)
  }

  private def constructUrl(method: String, params: Seq[Tuple2[String, Any]]): Uri = {
    val allParams = baseParams ++ params

    (baseUrl / method).addParams(allParams)
  }

}