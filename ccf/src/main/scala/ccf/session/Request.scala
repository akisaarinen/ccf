package ccf.session

import ccf.transport.Request

object JoinRequest {
  def apply(channelId: ChannelId)(s: Session): Request = Request(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString, "type" -> "channel/join"),
    Some(Map("channelId" -> channelId.toString))
  )
}
