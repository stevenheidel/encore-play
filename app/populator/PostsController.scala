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
import populator.models._

object PostsController extends Controller {

  val actor = Akka.system.actorOf(Props[PopulatorActor])
  implicit val timeout: Timeout = Timeout(1.seconds)

  // Single post info
  def get(post_id: String) = Action.async {
    val ip = InstagramPhoto.findOne(post_id)
    val yv = YoutubeVideo.findOne(post_id)

    for {
      instagram <- ip
      youtube <- yv
    } yield {
      val post = instagram orElse youtube

      post match {
        case Some(x) => Ok(x)
        case None => NotFound
      }
    }
  }

  def getList(event_id: Long) = Action.async {
    val ips = InstagramPhoto.findAll(event_id)
    val yvs = YoutubeVideo.findAll(event_id)

    for {
      instagrams <- ips
      youtubes <- yvs
    } yield {
      Ok(Json.obj(
        "posts" -> Json.toJson(youtubes ++ instagrams)
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

}