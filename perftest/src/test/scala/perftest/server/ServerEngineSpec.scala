package perftest.server

import org.specs.Specification
import org.specs.mock.{Mockito, MockitoMatchers}
import ccf.transport.Request
import ccf.session.AbstractRequest


class ServerEngineSpec extends Specification with Mockito with MockitoMatchers  {
  "ServerEngine" should {
    class TestServerEngine extends ServerEngine {
      override def fatalError(msg: String) {}
    }

    val engine = spy(new TestServerEngine)

    "decode None as an error" in {
      engine.decodeRequest(None)
      there was one(engine).fatalError("Unable to decode request")
    }

    "decode request header type" in {
      val request = mock[Request]

      "None as an error" in {
        request.header("type") returns None
        engine.decodeRequest(Some(request))
        there was one(engine).fatalError("No request type given")
      }

      "Unknown request type as an error" in {
        val requestType: String = "Unknown"
        request.header("type") returns Some(requestType)
        engine.decodeRequest(Some(request))
        there was one(engine).fatalError("Unknown request type: " + requestType)
      }

      "AbstractRequest.joinRequestType successfully" in {
        request.header("type") returns Some(AbstractRequest.joinRequestType)
        engine.decodeRequest(Some(request))
        there was no(engine).fatalError(any[String])
      }

      "AbstractRequest.partRequestType successfully" in {
        request.header("type") returns Some(AbstractRequest.partRequestType)
        engine.decodeRequest(Some(request))
        there was no(engine).fatalError(any[String])
      }
    }
  }
}
