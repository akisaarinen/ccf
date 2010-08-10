package ccf.session

import ccf.transport.{Request, Response}
import ccf.OperationContext
import ccf.operation.Operation

case object Shutdown

trait Message {
  def send(s: Session): (Session, Option[Response])
  protected def send(s: Session, request: Request, channels: Set[ChannelId]): (Session, Option[Response]) = {
    val nextSession = s.next(channels)
    val response = s.connection.send(request)
    (nextSession, response)
  }
}

case class Join(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = 
    if (!s.channels(channelId)) send(s, new JoinRequest().create(s, channelId), s.channels + channelId) else (s, None)
}
case class Part(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = 
    if (s.channels(channelId)) send(s, new PartRequest().create(s, channelId), s.channels - channelId) else (s, None)
}
case class InChannelMessage(requestType: String, channelId: ChannelId, content: Option[Any]) extends Message {
  def send(s: Session): (Session, Option[Response]) =
    if (s.channels(channelId)) send(s, new InChannelRequest().create(s, requestType, channelId, content), s.channels) else (s, None)
}
case class OperationContextMessage[T <: Operation](channelId: ChannelId, context: OperationContext[T]) extends Message {
  def send(s: Session): (Session, Option[Response]) = {
    if (s.channels(channelId)) send(s, new OperationContextRequest().create(s, channelId, context), s.channels) else (s, None)
  }
}
