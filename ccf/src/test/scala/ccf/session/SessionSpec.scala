package ccf.session

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.transport.Connection

object SessionSpec extends Specification with Mockito {
  val existingChannelId = ChannelId.randomId
  val newChannelId = ChannelId.randomId
  val channels = Set(existingChannelId)
  val session = Session(mock[Connection], mock[Version], mock[ClientId], 0, channels)
  "Session next(...)" should {
    "produce new session with incremented seqId" in {
      session.next(Set()).seqId must equalTo(1)
    }
    "produce new session with updated channel list" in {
      session.next(Set(newChannelId)).channels must equalTo(Set(newChannelId))
    }
  }
}
