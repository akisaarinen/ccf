package ccf.session

import ccf.transport.{Request, Response}

case object Shutdown

trait Message {
  def send(s: Session): (Session, Option[Response])
  protected def sendRequest(request: Session => Request, session: Session): Option[Response] = session.connection.send(request(session))
}

case class Join(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = if (!s.channels(channelId)) {
    val nextSession = s.next(s.channels + channelId)
    val response = sendRequest(JoinRequest(channelId), s)
    (nextSession, response)
  } else { (s, None) }
}
case class Part(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = if (s.channels(channelId)) {
    val nextSession = s.next(s.channels - channelId)
    val response = sendRequest(PartRequest(channelId), s)
    (nextSession, response)
  } else { (s, None) }
}
case class Operation(requestType: String, channelId: ChannelId, content: Option[Any]) extends Message {
  override def send(s: Session): (Session, Option[Response]) = {
    val nextSession = s.next(s.channels)
    val response = sendRequest(OperationRequest(requestType, channelId, content), s)
    (nextSession, response)
  }
}
