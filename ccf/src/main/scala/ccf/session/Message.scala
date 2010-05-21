package ccf.session

import ccf.transport.{Request, Response}

case object Shutdown

trait Message {
  def send(s: Session): (Session, Option[Response])
  protected def send(s: Session, request: Request, channels: Set[ChannelId]) = {
    val nextSession = s.next(channels)
    val response = s.connection.send(request)
    (nextSession, response)
  }
}

case class Join(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = 
    if (!s.channels(channelId)) send(s, JoinRequest(s, channelId), s.channels + channelId) else (s, None)
}
case class Part(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = 
    if (s.channels(channelId)) send(s, PartRequest(s, channelId), s.channels - channelId) else (s, None)
}
case class InChannelMessage(requestType: String, channelId: ChannelId, content: Option[Any]) extends Message {
  def send(s: Session): (Session, Option[Response]) = send(s, InChannelRequest(s, requestType, channelId, content), s.channels)
}
