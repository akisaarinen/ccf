package ccf.transport

import org.specs.Specification

object ChannelIdSpec extends Specification {
  "Random ChannelId" should {
    val clientId = ChannelId.randomId
    "not be equal to another random ChannelId" in {
      clientId must not equalTo(ChannelId.randomId)
    }
  }
}
