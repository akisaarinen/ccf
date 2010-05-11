package ccf.session

import java.util.UUID

case class ClientId(id: UUID)

object ClientId {
  def randomId = ClientId(UUID.randomUUID)
}
