package populator.algorithms

import play.api._
import lastfm.entities.{Event, Venue}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object FlickrPopulator {
  def populate(event: Event): Future[Int] = {
    Future { 0 }
  }
}