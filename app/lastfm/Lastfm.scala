package lastfm

object Lastfm {
  // The maximum radius to search on calls with latitude and longitude
  val maxDistance = 100.0

  // Interfaces for other packages
  def getEvent = lastfm.actions.SingleEvent.get _
  def getEvents = lastfm.actions.SingleEvent.multi _
}