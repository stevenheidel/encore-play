package populator.algorithms

import play.api._
import lastfm.entities.{Event, Venue}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.util.{Success, Failure}

object YoutubePopulator {
  def populate(event: Event): Future[Int] = {
    Future { 0 }
  }
}