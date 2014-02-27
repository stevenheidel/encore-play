package lastfm.models

import play.api.libs.json._

case class LastfmEvent(json: JsValue) {
  require(LastfmEvent.isValid(json))
}

object LastfmEvent {

  def isValid(json: JsValue) = false

}