package lastfm.responses

import play.api._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import lastfm.helpers.JsonConversions._
import lastfm.entities.Artist

case class SearchResult(
  artistMatches: Seq[Artist],
  term: String
  // and a number of other ignored results
)

object SearchResult {
  // Convert from Last.fm format
  implicit val searchResultRead: Reads[SearchResult] = (
    (
      // If it's a single object, convert to a sequence of one item
      (__ \ "results" \ "artistmatches" \ "artist").read[Seq[Artist]] orElse
      (__ \ "results" \ "artistmatches" \ "artist").read[Artist].map(Seq(_)) orElse
      // Or there might be nothing at all
      Reads.pure(Seq())
    ) ~
    (__ \ "results" \ "@attr" \ "for").read[String]
  )(SearchResult.apply _)
}