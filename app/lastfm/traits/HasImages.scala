package lastfm.traits

import lastfm.entities.Image

trait HasImages {
  val image: Seq[Image]

  // Only find largest image if necessary
  lazy val largestImage: Image = {
    val order = Seq("small", "medium", "large", "extralarge", "mega")

    def sortFn(a: Image, b: Image) = {
      order.indexOf(a.size) < order.indexOf(b.size)
    }

    image.sortWith(sortFn).last
  }
}