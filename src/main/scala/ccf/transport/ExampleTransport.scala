package ccf.transport

import ccf.operation.Operation
import ccf.messaging.Message
import ccf.server.Server
import scala.actors.Actor._

trait ExampleTransportListener {
  def onInitialize(server: Server[_]): Unit
  def onJoin(clientId: ClientId, channelId: ChannelId): Any
  def onQuit(clientId: ClientId, channelId: ChannelId): Unit
  def onSync(clientId: ClientId, channelId: ChannelId): Unit
  def onMsg(clientId: ClientId, channelId: ChannelId, msg: Message[Operation]): Unit
}

class ExampleTransport(listener: ExampleTransportListener) extends TransportActor {
  start
  def act() = loop { react {
    case Event.Join(client, channel) => {
      val state = listener.onJoin(client, channel)
      reply(Event.State(client, channel, state))
    }
    case Event.Quit(client, channel) => {
      listener.onQuit(client, channel)
      reply(Event.Ok())
    }
    case Event.Sync(client, channel) => {
      listener.onSync(client, channel)
      reply(Event.Ok())
    }
    case Event.Msg(client, channel, msg) => {
      listener.onMsg(client, channel, msg)
      reply(Event.Ok())
    }
    case _ => reply(Event.Error("Unknown message"))
  }}

  def initialize(server: Server[_]) = listener.onInitialize(server)
}
