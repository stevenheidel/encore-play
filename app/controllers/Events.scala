package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.Future

import lastfm.UrlBuilder

object Events extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def get(id: Long) = Action.async {
    WS.url(UrlBuilder.event_getInfo(id)).get().map { response =>
      Ok(response.json)
    }
  }

}