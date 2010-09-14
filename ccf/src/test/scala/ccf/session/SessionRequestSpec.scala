package ccf.session

import org.specs.Specification
import org.specs.mock.Mockito
import java.util.UUID
import ccf.OperationContext
import ccf.tree.operation.NoOperation
import ccf.transport.{TransportRequestType, TransportResponse, TransportRequest}

class SessionRequestSpec extends Specification with Mockito {
  val session = mock[Session]
  session.seqId returns 1
  session.version returns Version(2, 3)
  session.clientId returns ClientId(new UUID(4, 5))
  val channelId = new ChannelId(new UUID(6, 7))
  val commonTransportHeaders = Map("sequenceId" -> session.seqId.toString, "version" -> session.version.toString,
      "clientId" -> session.clientId.id.toString, "channelId" -> channelId.toString)
  val channelIdContent = Some(Map("channelId" -> channelId.toString))
  val responseResult = Some("response result")
  val expectedReason = "expected reason"
  val emptyTransportRequest = TransportRequest(Map(), None)
  val transportRequestWithUnknownType = TransportRequest(Map("type" -> "wrong type"), None)

  "Session request factory" should {
    "not create request without type" in {
      SessionRequest.sessionRequest(emptyTransportRequest) must throwAn[RuntimeException]
    }

    "not create request with unknown type" in {
      SessionRequest.sessionRequest(transportRequestWithUnknownType) must throwAn[RuntimeException]
    }
  }

  "JoinRequest" should {
    "not construct without type" in {
      new JoinRequest(emptyTransportRequest) must throwAn[IllegalArgumentException]
    }
    
    "not construct with unknown type" in {
      new JoinRequest(transportRequestWithUnknownType) must throwAn[IllegalArgumentException]
    }

    val joinRequest = new JoinRequest(session, channelId)
    val expectedTransportRequest = TransportRequest(commonTransportHeaders + ("type" -> TransportRequestType.join), channelIdContent)

    "be constructible by session request factory" in {
      SessionRequest.sessionRequest(expectedTransportRequest) must haveClass[JoinRequest]
    }

    "have correct transport request" in {
      joinRequest.transportRequest mustEqual expectedTransportRequest
    }

    "return correct success response" in {
      val expectedTransportResponse = TransportResponse(joinRequest.transportRequest.headers, SessionRequest.successResponseContent)
      val expectedResponse = JoinResponse(expectedTransportResponse, Left(Success(joinRequest, responseResult)))
      
      joinRequest.successResponse(responseResult) mustEqual expectedResponse
    }

    "return correct failure response" in {
      val expectedTransportResponse = TransportResponse(joinRequest.transportRequest.headers, SessionRequest.failureResponseContent(expectedReason))
      val expectedResponse = JoinResponse(expectedTransportResponse, Right(Failure(joinRequest, expectedReason)))

      joinRequest.failureResponse(expectedReason) mustEqual expectedResponse
    }
  }

  "PartRequest" should {
    "not construct without type" in {
      new PartRequest(emptyTransportRequest) must throwAn[IllegalArgumentException]
    }

    "not construct with unknown type" in {
      new PartRequest(transportRequestWithUnknownType) must throwAn[IllegalArgumentException]
    }

    val partRequest = new PartRequest(session, channelId)
    val expectedTransportRequest = TransportRequest(commonTransportHeaders + ("type" -> TransportRequestType.part), channelIdContent)

    "be constructible by session request factory" in {
      SessionRequest.sessionRequest(expectedTransportRequest) must haveClass[PartRequest]
    }

    "have correct transport request" in {
      partRequest.transportRequest mustEqual expectedTransportRequest
    }

    "return correct success response" in {
      val expectedTransportResponse = TransportResponse(partRequest.transportRequest.headers, SessionRequest.successResponseContent)
      val expectedResponse = PartResponse(expectedTransportResponse, Left(Success(partRequest, responseResult)))

      partRequest.successResponse(responseResult) mustEqual expectedResponse
    }

    "return correct failure response" in {
      val expectedTransportResponse = TransportResponse(partRequest.transportRequest.headers, SessionRequest.failureResponseContent(expectedReason))
      val expectedResponse = PartResponse(expectedTransportResponse, Right(Failure(partRequest, expectedReason)))

      partRequest.failureResponse(expectedReason) mustEqual expectedResponse
    }
  }

  "InChannelRequest" should {
    "not construct without type" in {
      new InChannelRequest(emptyTransportRequest) must throwAn[IllegalArgumentException]
    }

    "not construct with session control request type" in {
      TransportRequestType.sessionControlTypes map { (requestType) => {
        val sessionControlTransportRequest = TransportRequest(Map("type" -> requestType), None)
        new InChannelRequest(sessionControlTransportRequest) must throwAn[IllegalArgumentException]
        }
      }
    }

    val transportRequestType = "requestType"
    val transportRequestContent = Some(Map("content" -> "data"))
    val inChannelRequest = new InChannelRequest(session, transportRequestType, channelId, transportRequestContent)
    val expectedTransportRequest = TransportRequest(commonTransportHeaders + ("type" -> transportRequestType), transportRequestContent)

    "have correct transport request" in {
      inChannelRequest.transportRequest mustEqual expectedTransportRequest
    }

    "return correct success response" in {
      val expectedTransportResponse = TransportResponse(inChannelRequest.transportRequest.headers, SessionRequest.successResponseContent)
      val expectedResponse = InChannelResponse(expectedTransportResponse, Left(Success(inChannelRequest, responseResult)))

      inChannelRequest.successResponse(responseResult) mustEqual expectedResponse
    }

    "return correct failure response" in {
      val expectedTransportResponse = TransportResponse(inChannelRequest.transportRequest.headers, SessionRequest.failureResponseContent(expectedReason))
      val expectedResponse = InChannelResponse(expectedTransportResponse, Right(Failure(inChannelRequest, expectedReason)))

      inChannelRequest.failureResponse(expectedReason) mustEqual expectedResponse
    }
  }

  "OperationContextRequest" should {
    "not construct without type" in {
      new OperationContextRequest(emptyTransportRequest) must throwAn[IllegalArgumentException]
    }

    "not construct with unknown type" in {
      new OperationContextRequest(transportRequestWithUnknownType) must throwAn[IllegalArgumentException]
    }

    val context = OperationContext(NoOperation(), 1, 2)
    val operationContextRequest = new OperationContextRequest(session, channelId, context)
    val transportRequestContent = Some(context.encode)
    val expectedTransportRequest = TransportRequest(commonTransportHeaders + ("type" -> TransportRequestType.context), transportRequestContent)

    "be constructible by session request factory" in {
      SessionRequest.sessionRequest(expectedTransportRequest) must haveClass[OperationContextRequest]
    }

    "have correct transport request" in {
      operationContextRequest.transportRequest mustEqual expectedTransportRequest
    }

    "return correct success response" in {
      val expectedTransportResponse = TransportResponse(operationContextRequest.transportRequest.headers, SessionRequest.successResponseContent)
      val expectedResponse = OperationContextResponse(expectedTransportResponse, Left(Success(operationContextRequest, responseResult)))

      operationContextRequest.successResponse(responseResult) mustEqual expectedResponse
    }

    "return correct failure response" in {
      val expectedTransportResponse = TransportResponse(operationContextRequest.transportRequest.headers, SessionRequest.failureResponseContent(expectedReason))
      val expectedResponse = OperationContextResponse(expectedTransportResponse, Right(Failure(operationContextRequest, expectedReason)))

      operationContextRequest.failureResponse(expectedReason) mustEqual expectedResponse
    }
  }
}
