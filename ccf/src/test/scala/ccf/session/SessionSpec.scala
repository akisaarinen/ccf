package ccf.session

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.transport.Connection

object SessionSpec extends Specification with Mockito {
  val connection = mock[Connection]
  val version = Version(1, 2)
  val clientId = ClientId.randomId
  val existingChannelId = ChannelId.randomId
  val newChannelId = ChannelId.randomId
  val channels = Set(existingChannelId)
  val session = Session(connection, version, clientId, 0, channels)
  "Session next(...)" should {
    "produce new session with incremented seqId" in {
      session.next(Set()).seqId must equalTo(1)
    }
    "produce new session with updated channel list" in {
      session.next(Set(newChannelId)).channels must equalTo(Set(newChannelId))
    }
  }
  "Session send(...)" should {
    "send valid Join message, sending correct Request and producing correct Session" in {
      val joinRequest = JoinRequest(newChannelId)(session)
      val joinMessage = Join(newChannelId)
      connection.send(joinRequest) returns None
      val nextSession = session.send(joinMessage)

      nextSession.seqId must equalTo(1)
      nextSession.channels must equalTo(Set(existingChannelId, newChannelId))
      connection.send(joinRequest) was called
    }
    "send Part message, sending correct Request and producing correct Session" in {
      val partRequest = PartRequest(existingChannelId)(session)
      val partMessage = Part(existingChannelId)
      connection.send(partRequest) returns None
      val nextSession = session.send(partMessage)

      nextSession.seqId must equalTo(1)
      nextSession.channels must equalTo(Set())
      connection.send(partRequest) was called
    }
  }
}
