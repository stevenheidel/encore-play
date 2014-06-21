/*import actors.{Tick, CacheUpdater}
import akka.actor.Props
import play.api._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Starting scheduled actors...")

    val cacheUpdateActor = Akka.system.actorOf(Props[CacheUpdater], name = "cache_updater")
    Akka.system.scheduler.schedule(0.seconds, 5.minutes, cacheUpdateActor, Tick)
  }
}
*/