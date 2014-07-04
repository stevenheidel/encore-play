package lastfm.helpers

import play.api.libs.ws._
import play.api.libs.json.{JsValue => PlayJsValue, Json => PlayJson}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.xml._

object XmlConvert {
  def convert(response: WSResponse): PlayJsValue = try {
      response.json
    } catch {
      case _: com.fasterxml.jackson.core.JsonParseException => PlayJson.parse(jsonize(response.body))
    }

  private def jsonize(body: String): String = {
    // First correct the XML so that it's valid
    val troubleRegex = """http:\/\/www\.last\.fm\/music\/.*\/\+events""".r
    val badUrl = troubleRegex.findFirstIn(body).get
    val goodUrl = badUrl.replace("&", "%26")
    val xml = body.replace(badUrl, goodUrl)

    // Convert to JSON using lift
    var json = Xml.toJson(XML.loadString(xml))

    // Correct JSON, start by taking out root element
    val correctedJson = (json \ "lfm") transform {
      // And putting attributes under a header
      case JField("events", JObject(xs)) => JField("events", JObject(xs.last :: List(JField("@attr", xs.init))))
      // And dealing with images
      case JObject(xs) => JObject(fixImages(xs))
    } remove {
      // And removing status
      _ == JField("status", "ok")
    }

    compact(render(correctedJson))
  }

  // Take all the image, size pairs and format them under an "image" array
  private def fixImages(list: List[JField]): List[JField] = {
    // Get the correct list to be put under "image"
    val imagesAndSizes: List[JField] = list.collect { 
      case JField("image", x) => JField("#text", x)
      case JField("size", x) => JField("size", x)
    }

    // If only the sizes were found, need to add #texts
    val imagesAndSizesComplete: List[JField] = if (imagesAndSizes.headOption.map(_.name == "size").getOrElse(false)) {
      List.fill(5)(JField("#text", "")).zip(imagesAndSizes).flatMap(p => List(p._1, p._2))
    } else {
      imagesAndSizes
    }
    
    // Convert to proper format, split between #text and size
    val correctedImages: List[JObject] = imagesAndSizesComplete.grouped(2).map(JObject(_)).toList

    // Remove old images and sizes
    val filteredList: List[JField] = list.filter {
      case JField(x, _) => x != "image" && x != "size"
    }

    // Add the "image" field to the rest
    val image: JField = JField("image", correctedImages)
    if (! correctedImages.isEmpty) (image :: filteredList) else list
  }
}