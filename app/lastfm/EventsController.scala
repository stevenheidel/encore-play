package lastfm

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import lastfm.actions._
import play.api.libs.json._
import scala.util.{Success, Failure}
import lastfm.helpers.Pagination

object EventsController extends Controller {
  
  // UNIMPLEMENTED
  def pastEvents(latitude: Double, longitude: Double, radius: Double, date: String) = Action {
    Ok(Json.obj("events" -> JsArray()))
  }

  def todaysEvents(latitude: Double, longitude: Double, radius: Double, date: String) = Action.async {
    GeoUpcoming.get(latitude, longitude, radius * Lastfm.maxDistance).map { 
      case (total, list) => Ok(Json.obj(
        "events" -> Json.toJson(list.filter(_.isToday(date)))
      ))
    }
  }

  def futureEvents(latitude: Double, longitude: Double, radius: Double, page: Int, limit: Int, date: String) = Action.async {
    GeoUpcoming.get(latitude, longitude, radius * Lastfm.maxDistance, Pagination(limit, page)).map { 
      case (total, list) => {
        // On getting the first page of results, precache the following pages so 'load more' goes faster
        if (page == 1) {
          val maxPage = Math.max(10, total/limit)

          (2 to maxPage).map { x =>
            GeoUpcoming.get(latitude, longitude, radius * Lastfm.maxDistance, Pagination(limit, x))
          }
        }

        Ok(Json.obj(
          "total" -> total,
          "events" -> Json.toJson(list.filter(_.isFuture(date)))
        ))
      }
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