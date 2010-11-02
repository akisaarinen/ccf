package ccf.server

import ccf.session.{ClientId, ChannelId}

trait NotifyingInterceptor {
  def notify(clientId: ClientId, channelId: ChannelId): Unit
}