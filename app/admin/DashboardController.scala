package admin

import play.api._
import play.api.mvc._
import users.User
import scala.concurrent.ExecutionContext.Implicits.global
import com.restfb.DefaultFacebookClient
import com.restfb.types.{User => FBUser}

object DashboardController extends Controller {
  def updateEmail(user: User): Unit = {
    if (user.oauth.isDefined && !user.email.isDefined) {
      val client = new DefaultFacebookClient(user.oauth.get, Play.current.configuration.getString("facebook.app_secret").get)
      val fb: FBUser = client.fetchObject("me", classOf[FBUser]);

      val email = fb.getEmail()
      val updatedUser = user.copy(email = Some(email))

      User.update(updatedUser)
    }
  }

  def userList = Action.async {
    User.all().map { users =>
      // Update the emails
      users.map(updateEmail)

      Ok(views.html.dashboard.user_list(users))
    }
  }
}