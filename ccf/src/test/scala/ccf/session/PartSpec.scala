package ccf.session

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.transport.Connection

object PartSpec extends Specification with Mockito {
  val connection = mock[Connection]
  val version = Version(1, 2)
  val clientId = ClientId.randomId
  val existingChannelId = ChannelId.randomId
  val newChannelId = ChannelId.randomId
  val session = Session(connection, version, clientId, 0, Set(existingChannelId))
  "Part message" should {
    val partRequest = PartRequest(existingChannelId)(session)
    val partMessage = Part(existingChannelId)
    connection.send(partRequest) returns None
    "invoke #send with a message and return a valid new session" in {
      val (nextSession: Session, _) = partMessage.send(session)
      nextSession.seqId must be equalTo(1)
      nextSession.channels must be equalTo(Set())
      connection.send(partRequest) was called
    }
    "take no action if not a member of a channel" in {
      val partInvalidChannelMessage = Part(newChannelId)
      val (nextSession: Session, _) = partInvalidChannelMessage.send(session)
      nextSession must be equalTo(session)
      connection.send(partRequest) wasnt called
    }
  }
}
