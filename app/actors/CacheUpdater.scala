package actors

import akka.actor.Actor

case object Tick

class CacheUpdater extends Actor {
  def receive = {
    case Tick => println("Hello")
  }
}
