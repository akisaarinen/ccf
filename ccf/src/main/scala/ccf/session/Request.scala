package ccf.session

import ccf.transport.Request

object JoinRequest {
  def apply(channelId: ChannelId)(s: Session): Request = Request(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "type" -> "channel/join"),
    Some(Map("channelId" -> channelId.toString))
  )
}
object PartRequest {
  def apply(channelId: ChannelId)(s: Session): Request = Request(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "type" -> "channel/part"),
    Some(Map("channelId" -> channelId.toString))
  )
}
object InChannelRequest {
  def apply(requestType: String, channelId: ChannelId, content: Option[Any])(s: Session): Request = Request(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "channelId" -> channelId.toString, "type" -> requestType),
    content
  )
}
