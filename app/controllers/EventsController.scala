package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import lastfm.actions._
import play.api.libs.json._
import scala.util.{Success, Failure}
import lastfm.Helpers._

object EventsController extends Controller {
  
  def pastEvents(latitude: Double, longitude: Double, radius: Double) = TODO

  def todaysEvents(latitude: Double, longitude: Double, radius: Double) = TODO

  def futureEvents(latitude: Double, longitude: Double, radius: Double, page: Int, limit: Int) = Action.async {
    GeoUpcoming.get(latitude, longitude, radius * MaxDistance, page, limit).map { 
      case (total, list) => Ok(Json.obj(
        "total" -> total,
        "events" -> Json.toJson(list)
      ))
    }
  }

  def singleEvent(event_id: Long) = Action.async {
    SingleEvent.get(event_id).map(event => Ok(Json.toJson(event)))
  }

  def seatgeekUrl(event_id: Long) = Action.async {
    for {
      event <- SingleEvent.get(event_id)
      seatgeekUrl <- seatgeek.TicketsUrl.get(event)
    } yield {
      Ok(Json.obj(
        "seatgeek_url" -> seatgeekUrl
      ))
    }
  }

}