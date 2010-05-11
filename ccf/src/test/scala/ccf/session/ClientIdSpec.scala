package ccf.session

import org.specs.Specification

object ClientIdSpec extends Specification {
  "Random ClientId" should {
    val clientId = ClientId.randomId
    "not be equal to another random ClientId" in {
      clientId must not equalTo(ClientId.randomId)
    }
  }
}
