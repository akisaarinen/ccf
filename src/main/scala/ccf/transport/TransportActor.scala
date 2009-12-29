package ccf.transport

import ccf.server.Server
import scala.actors.Actor

trait TransportActor extends Actor {
  def initialize(server: Server[_]): Unit
}
