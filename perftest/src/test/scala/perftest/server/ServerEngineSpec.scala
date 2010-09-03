package perftest.server

import org.specs.Specification
import org.specs.mock.Mockito


class ServerEngineSpec extends Specification with Mockito  {
  "ServerEngine" should {
    class TestServerEngine extends ServerEngine {
      override def fatalError(msg: String) {}
    }

    val engine = spy(new TestServerEngine)

    "decode None as an error" in {
      engine.decodeRequest(None)
      there was one(engine).fatalError("Unable to decode request")
    }
  }
}
