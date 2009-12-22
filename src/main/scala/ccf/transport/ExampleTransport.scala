package ccf.transport

import ccf.operation.Operation
import ccf.messaging.ConcurrentOperationMessage
import scala.actors.Actor._

trait ExampleTransportListener {
  def onJoin(clientId: ClientId, channelId: ChannelId): Any
  def onQuit(clientId: ClientId, channelId: ChannelId): Unit
  def onSync(clientId: ClientId, channelId: ChannelId): Unit
  def onMsg(clientId: ClientId, channelId: ChannelId, msg: ConcurrentOperationMessage[Operation]): Unit
}

class ExampleTransport(listener: ExampleTransportListener) extends TransportActor {
  start
  def act() = loop { react {
    case Event.Join(_, client, channel) => {
      val state = listener.onJoin(client, channel)
      reply(Event.State(client, channel, state))
    }
    case Event.Quit(client, channel) => {
      listener.onQuit(client, channel)
      reply(Event.Ok())
    }
    case Event.Sync(_, client, channel) => {
      listener.onSync(client, channel)
      reply(Event.Ok())
    }
    case Event.Msg(_, client, channel, msg) => {
      listener.onMsg(client, channel, msg)
      reply(Event.Ok())
    }
    case _ => reply(Event.Error("Unknown message"))
  }}

}
