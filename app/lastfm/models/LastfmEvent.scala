package lastfm.models

import play.api._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class LastfmEvent(json: JsValue) {
  require(LastfmEvent.isValid(json))

  def transform: EncoreEvent = {
    EncoreEvent(json)
  }
}

object LastfmEvent {

  def isValid(json: JsValue): Boolean = {
    val validator = (__ \ "event").read[JsValue]

    validator.reads(json).fold(
      valid = (_ => true),
      invalid = { err =>
        err.foreach { e =>
          Logger.error("Lastfm Event failed to validate path: " + e._1)
        }
        false
      }
    )
  }

}