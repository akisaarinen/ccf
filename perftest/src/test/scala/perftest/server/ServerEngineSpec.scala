package perftest.server

import org.specs.Specification
import org.specs.mock.{Mockito, MockitoMatchers}
import ccf.transport.Request
import ccf.session.AbstractRequest
import collection.immutable.HashMap
import ccf.tree.operation.TreeOperationDecoder


class ServerEngineSpec extends Specification with Mockito with MockitoMatchers  {
  "ServerEngine" should {
    val operationDecoderMock = mock[TreeOperationDecoder]

    class TestServerEngine extends ServerEngine {
      override def fatalError(msg: String) {}
      override def newOperationDecoder = operationDecoderMock
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

      "AbstractRequest.contextRequestType successfully" in {
        request.header("type") returns Some(AbstractRequest.contextRequestType)
        val expectedOperation: Any = "ExpectedOperation"
        val content: Option[Map[String, Any]] = Some(Map[String, Any]("op" -> expectedOperation, "localMsgSeqNo" -> 0, "remoteMsgSeqNo" -> 0))
        content.get("op") mustBe expectedOperation
        request.content returns content
        engine.decodeRequest(Some(request))
        there was one(operationDecoderMock).decode(expectedOperation)
        there was no(engine).fatalError(any[String])
      }

    }
  }
}
