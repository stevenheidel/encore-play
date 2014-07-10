package admin

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object PingController extends Controller with MongoController {
  def collection: JSONCollection = db.collection[JSONCollection]("ping")

  def ping = Action.async {
    collection.find(Json.obj("ping" -> "ping")).one[JsValue].map { p =>
      Ok(p.get)
    }
  }
}