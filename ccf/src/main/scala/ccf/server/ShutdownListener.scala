package ccf.server

import ccf.session.ChannelId

trait ShutdownListener {
  def shutdown(channelId: ChannelId, reason: String) {}
}