package ccf.session

import java.util.UUID

case class ChannelId(id: UUID)

object ChannelId {
  def randomId = ChannelId(UUID.randomUUID)
}
