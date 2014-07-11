package populator

import play.api._
import play.api.mvc._
import play.api.libs.json._
import populator.actors._
import akka.actor._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object PostsController extends Controller {

  val actor = Akka.system.actorOf(Props[BasePopulator])
  implicit val timeout: Timeout = Timeout(1.seconds)

  def getList(event_id: Long) = Action.async {
    populator.models.InstagramPhoto.findAll(event_id).map { posts =>
      Ok(Json.obj(
        "posts" -> Json.toJson(posts)
      ))
    }
  }

  def startPopulating(event_id: Long) = Action {
    actor ! Start(event_id)
    Ok(Json.obj("response" -> "success"))
  }

  def checkPopulating(event_id: Long) = Action.async {
    ask(actor, Check(event_id)).mapTo[Boolean].map { response =>
      Ok(Json.obj("response" -> response.toString))
    }
  }

  // DELETE WHEN FINISHED
  def test = Action {
    Ok("Test")
  }

}