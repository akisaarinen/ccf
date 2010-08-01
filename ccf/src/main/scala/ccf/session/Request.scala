package ccf.session

import ccf.transport.Request
import ccf.OperationContext
import ccf.operation.Operation

abstract class AbstractRequest {
  protected def request(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): Request = Request(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "channelId" -> channelId.toString, "type" -> requestType),
    content
  )
}

object AbstractRequest {
  val joinRequestType = "channel/join"
  val partRequestType = "channel/part"
  val contextRequestType = "channel/context"
}

abstract class SessionControlRequest extends AbstractRequest {
  protected def request(s: Session, channelId: ChannelId): Request = request(s, requestType, channelId, content(channelId))
  private def content(channelId: ChannelId) = Some(Map("channelId" -> channelId.toString))
  protected val requestType: String
}

class JoinRequest extends SessionControlRequest {
  def create(s: Session, channelId: ChannelId): Request = request(s, channelId)
  protected val requestType = AbstractRequest.joinRequestType
}

class PartRequest extends SessionControlRequest  {
  def create
  (s: Session, channelId: ChannelId): Request = request(s, channelId)
  protected val requestType = AbstractRequest.partRequestType
}

class InChannelRequest extends AbstractRequest {
  def create(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): Request =
    request(s, requestType, channelId, content)
}

class OperationContextRequest[T <: Operation] extends InChannelRequest {
  def create(s: Session, channelId: ChannelId, context: OperationContext[T]): Request =
    request(s, requestType, channelId, Some(context.encode))
  protected val requestType = AbstractRequest.contextRequestType
}


