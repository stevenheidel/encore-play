package populator

import play.api._
import play.api.mvc._
import play.api.libs.json._

object PostsController extends Controller {

  // UNIMPLEMENTED
  def getList(event_id: Long) = Action {
    Ok(Json.parse("""{"posts": []}"""))
  }

  // UNIMPLEMENTED
  def startPopulating(event_id: Long) = Action {
    Ok(Json.parse("""{"response": "success"}"""))
  }

  // UNIMPLEMENTED
  def checkPopulating(event_id: Long) = Action {
    Ok(Json.parse("""{"response": "false"}"""))
  }

  // DELETE WHEN FINISHED
  def test = Action.async {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    import lastfm.actions.SingleEvent
    import populator.algorithms._

    for {
      event <- SingleEvent.get(1920146)
    } yield {
      Instagrams.populate(event)
      Ok("Done.")
    }
  }

}