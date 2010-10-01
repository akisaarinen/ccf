/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ccf.session

import org.specs.Specification
import org.specs.mock.Mockito
import java.util.UUID
import ccf.OperationContext
import ccf.tree.operation.NoOperation
import ccf.transport.{TransportResponse, TransportRequest}

class SessionRequestSpec extends Specification with Mockito {
  val session = mock[Session]
  session.seqId returns 1
  session.version returns Version(2, 3)
  session.clientId returns ClientId(new UUID(4, 5))
  val channelId = new ChannelId(new UUID(6, 7))
  val channelIdContent = Some(Map(SessionRequestFactory.ChannelIdKey -> channelId.toString))
  val responseResult = Some("response result")
  val expectedReason = "expected reason"
  val emptyTransportRequest = TransportRequest(Map(), None)
  val transportRequestWithUnknownType = TransportRequest(Map(SessionRequestFactory.TypeKey -> "wrong type"), None)

  "Session request factory" should {
    "not create request without type" in {
      SessionRequest(emptyTransportRequest) must throwAn[RuntimeException]
    }
  }

  "JoinRequest" should {
    "not construct without type" in {
      JoinRequest(emptyTransportRequest, channelId) must throwAn[IllegalArgumentException]
    }
    
    "not construct with unknown type" in {
      JoinRequest(transportRequestWithUnknownType, channelId) must throwAn[IllegalArgumentException]
    }

    commonChannelIdInContentSpec(SessionRequestFactory.JoinType)

    val joinRequest = JoinRequest(session, channelId)
    val expectedTransportRequest = TransportRequest(SessionRequestFactory.transportRequestHeaders(session, SessionRequestFactory.JoinType, channelId), channelIdContent)

    commonRequestSpec(joinRequest, expectedTransportRequest, JoinResponse.apply _)
  }

  "PartRequest" should {
    "not construct without type" in {
      PartRequest(emptyTransportRequest) must throwAn[IllegalArgumentException]
    }

    "not construct with unknown type" in {
      PartRequest(transportRequestWithUnknownType) must throwAn[IllegalArgumentException]
    }

    val partRequest = PartRequest(session, channelId)
    val expectedTransportRequest = TransportRequest(SessionRequestFactory.transportRequestHeaders(session, SessionRequestFactory.PartType, channelId), channelIdContent)

    commonRequestSpec(partRequest, expectedTransportRequest, PartResponse.apply _)
  }

  "InChannelRequest" should {
    "not construct without type" in {
      InChannelRequest(emptyTransportRequest) must throwAn[IllegalArgumentException]
    }

    "not construct with session control request type" in {
      SessionRequestFactory.SessionControlTypes map { (requestType) => {
        val sessionControlTransportRequest = SessionRequestFactory.transportRequest(session, requestType, channelId, None)
        InChannelRequest(sessionControlTransportRequest) must throwAn[IllegalArgumentException]
        }
      }
    }

    val transportRequestType = "requestType"
    val transportRequestContent = Some(Map("content" -> "data"))
    val inChannelRequest = InChannelRequest(session, transportRequestType, channelId, transportRequestContent)
    val expectedTransportRequest = TransportRequest(SessionRequestFactory.transportRequestHeaders(session, transportRequestType, channelId), transportRequestContent)
    
    commonRequestSpec(inChannelRequest, expectedTransportRequest, InChannelResponse.apply _)
  }

  "OperationContextRequest" should {
    "not construct without type" in {
      OperationContextRequest(emptyTransportRequest) must throwAn[IllegalArgumentException]
    }

    "not construct with unknown type" in {
      OperationContextRequest(transportRequestWithUnknownType) must throwAn[IllegalArgumentException]
    }

    val context = OperationContext(NoOperation(), 1, 2)
    val operationContextRequest = OperationContextRequest(session, channelId, context)
    val transportRequestContent = Some(context.encode)
    val expectedTransportRequest = TransportRequest(SessionRequestFactory.transportRequestHeaders(session, SessionRequestFactory.OperationContextType, channelId), transportRequestContent)

    commonRequestSpec(operationContextRequest, expectedTransportRequest, OperationContextResponse.apply _)
  }

  def commonChannelIdInContentSpec(requestType: String) {
    val headers = SessionRequestFactory.transportRequestHeaders(session, requestType, channelId)

    "not construct with empty transport request content" in {
      val transportRequestWithoutContent = TransportRequest(headers, None)
      SessionRequest(transportRequestWithoutContent) must throwAn[RuntimeException]
    }

    "not construct with invalid transport request content" in {
      val transportRequestWithInvalidContent = TransportRequest(headers, Some("##Invalid content##"))
      SessionRequest(transportRequestWithInvalidContent) must throwAn[RuntimeException]
    }

    "not construct without channel id in transport request content" in {
      val transportRequestWithoutChannelId = TransportRequest(headers, Some(Map(SessionRequestFactory.ChannelIdKey + "##" -> channelId.toString)))
      SessionRequest(transportRequestWithoutChannelId) must throwAn[RuntimeException]
    }

    "not construct with invalid channel id in transport request content" in {
      val transportRequestWithInvalidChannelId = TransportRequest(headers, Some(Map(SessionRequestFactory.ChannelIdKey -> "##Invalid channel id##")))
      SessionRequest(transportRequestWithInvalidChannelId) must throwAn[RuntimeException]
    }
  }

  def commonRequestSpec(request: SessionRequest, expectedTransportRequest: TransportRequest,
                        expectedResponseFactory: (TransportResponse, Either[Failure, Success]) => SessionResponse) {
    "be constructible by session request factory" in {
      SessionRequest(expectedTransportRequest) must equalTo(request)
    }

    "have correct transport request" in {
      request.transportRequest mustEqual expectedTransportRequest
    }

    "return correct success response without result" in {
      val expectedTransportResponse = TransportResponse(expectedTransportRequest.headers, SessionResponseFactory.successContent(None))
      val expectedResponse = expectedResponseFactory(expectedTransportResponse, Right(Success(request, None)))

      request.successResponse(None) mustEqual expectedResponse
    }

    "return correct success response with result" in {
      val expectedTransportResponse = TransportResponse(expectedTransportRequest.headers, SessionResponseFactory.successContent(responseResult))
      val expectedResponse = expectedResponseFactory(expectedTransportResponse, Right(Success(request, responseResult)))

      request.successResponse(responseResult) mustEqual expectedResponse
    }

    "return correct failure response" in {
      val expectedTransportResponse = TransportResponse(expectedTransportRequest.headers, SessionResponseFactory.failureContent(expectedReason))
      val expectedResponse = expectedResponseFactory(expectedTransportResponse, Left(Failure(request, expectedReason)))

      request.failureResponse(expectedReason) mustEqual expectedResponse
    }
  }
}
