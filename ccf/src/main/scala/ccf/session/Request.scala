package ccf.session

import ccf.transport.Request

abstract class AbstractRequest {
  protected def request(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): Request = Request(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "channelId" -> channelId.toString, "type" -> requestType),
    content
  )
}

abstract class SessionControlRequest extends AbstractRequest {
  protected def request(s: Session, requestType: String, channelId: ChannelId): Request = request(s, requestType, channelId, content(channelId))
  private def content(channelId: ChannelId) = Some(Map("channelId" -> channelId.toString))
}

object JoinRequest extends SessionControlRequest {
  def apply(s: Session, channelId: ChannelId): Request = request(s, "channel/join", channelId)
}

object PartRequest extends SessionControlRequest  {
  def apply(channelId: ChannelId)(s: Session): Request = request(s, "channel/part", channelId)
}

object InChannelRequest extends AbstractRequest {
  def apply(requestType: String, channelId: ChannelId, content: Option[Any])(s: Session): Request = 
    request(s, requestType, channelId, content)
}
