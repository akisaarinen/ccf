package ccf.session

import ccf.transport.{Request, Response}

case object Shutdown

trait Message {
  def send(s: Session): (Session, Option[Response])
}

case class Join(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = if (!s.channels(channelId)) {
    val nextSession = s.next(s.channels + channelId)
    val response = s.connection.send(JoinRequest(s, channelId))
    (nextSession, response)
  } else { (s, None) }
}
case class Part(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = if (s.channels(channelId)) {
    val nextSession = s.next(s.channels - channelId)
    val response = s.connection.send(PartRequest(channelId)(s))
    (nextSession, response)
  } else { (s, None) }
}
case class InChannelMessage(requestType: String, channelId: ChannelId, content: Option[Any]) extends Message {
  override def send(s: Session): (Session, Option[Response]) = {
    val nextSession = s.next(s.channels)
    val response = s.connection.send(InChannelRequest(requestType, channelId, content)(s))
    (nextSession, response)
  }
}
