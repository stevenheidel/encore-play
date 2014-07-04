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

}