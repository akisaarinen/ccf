package ccf.transport

import ccf.operation.Operation
import ccf.messaging.ConcurrentOperationMessage

object Event {
  case class Ok()
  case class Error(reason: String)

  case class Join(transport: TransportActor, clientId: ClientId, channelId: ChannelId)
  case class State[T](clientId: ClientId, channelId: ChannelId, state: T)
  case class Quit(clientId: ClientId, channelId: ChannelId)
  case class ShutdownChannel(channelId: ChannelId, reason: String)
  case class Sync(clientId: ClientId, channelId: ChannelId)
  case class Msg[T <: Operation](clientId: ClientId, channelId: ChannelId, msg: ConcurrentOperationMessage[T])
}
