package ccf.transport

import ccf.operation.Operation
import ccf.messaging.ConcurrentOperationMessage

object Event {
  case class Ok()
  case class Error(reason: String)

  case class Join(clientId: ClientId, channelId: ChannelId)
  case class State[T](clientId: ClientId, channelId: ChannelId, state: T)
  case class Quit(clientId: ClientId, channelId: ChannelId)
  case class Sync(transport: TransportActor, clientId: ClientId, channelId: ChannelId)
  case class Msg[T <: Operation](transport: TransportActor, clientId: ClientId, channelId: ChannelId, msg: ConcurrentOperationMessage[T])
}
