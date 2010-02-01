package textapp.server

import ccf.server.Server
import ccf.transport.{Event, TransportActor}
import ccf.tree.operation.TreeOperation
import scala.actors.Actor._

class TextAppTransportActor(onMessage: Event.Msg[TreeOperation] => Unit) extends TransportActor {
  start
  def act = loop { react {
    case msg: Event.Msg[TreeOperation] => onMessage(msg)
    case _ => 
  }}

  override def initialize(server: Server[_]) {}
}
