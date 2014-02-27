import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import lastfm.models.LastfmEvent

@RunWith(classOf[JUnitRunner])
class LastfmEventSpec extends Specification {

  "LastfmEvent" should {

    "create a valid event from Lastfm" in {
      LastfmEvent(validWithVenueJ) must beAnInstanceOf[LastfmEvent]
    }

    /*
    "create a valid event without a venue from Lastfm" in {

    } */

    "raise an error when given an invalid event from Lastfm" in {
      LastfmEvent(invalidJ) must throwA[IllegalArgumentException]
    }

  }

  // Event 98521
  val validWithVenue: String = """{"event":{"id":"98521","title":"Sepultura","artists":{"artist":["Sepultura","Leprosy"],"headliner":"Sepultura"},"venue":{"id":"8828969","name":"Auditório Morelos","location":{"geo:point":{"geo:lat":"24.0333333","geo:long":"-104.6666667"},"city":"Aguascalientes","country":"Mexico","street":"","postalcode":""},"url":"http://www.last.fm/venue/8828969+Audit%C3%B3rio+Morelos","website":"","phonenumber":"","image":[{"#text":"","size":"small"},{"#text":"","size":"medium"},{"#text":"","size":"large"},{"#text":"","size":"extralarge"},{"#text":"","size":"mega"}]},"startDate":"Sun, 18 Feb 2007 17:00:00","description":"<div class=\"bbcode\">Costo:  $260.00 Enero - $300.00 Febrero - $350.00 Dia del Evento</div>","image":[{"#text":"http://userserve-ak.last.fm/serve/34/33771451.jpg","size":"small"},{"#text":"http://userserve-ak.last.fm/serve/64/33771451.jpg","size":"medium"},{"#text":"http://userserve-ak.last.fm/serve/126/33771451.jpg","size":"large"},{"#text":"http://userserve-ak.last.fm/serve/252/33771451.jpg","size":"extralarge"}],"attendance":"5","reviews":"0","tag":"lastfm:event=98521","url":"http://www.last.fm/event/98521+Sepultura+at+Audit%C3%B3rio+Morelos+on+18+February+2007","website":"http://www.oidossordos.net/propa.php?eventos=2767&PHPSESSID=88a1d8318d3920919a19ccf9abb4c90b","tickets":"\n  ","cancelled":"0"}}"""
  val validWithVenueJ: JsValue = Json.parse(validWithVenue)
  // Event TODO: Find event # without venue
  val validWithoutVenue = None
  // Event 1
  val invalid: String = """{"error":6,"message":"Invalid event id supplied","links":[]}"""
  val invalidJ: JsValue = Json.parse(invalid)
}