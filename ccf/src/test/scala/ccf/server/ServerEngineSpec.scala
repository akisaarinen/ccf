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

package ccf.server

import org.specs.Specification
import org.specs.mock.{Mockito, MockitoMatchers}
import javax.activation.MimeType
import ccf.session._
import ccf.transport.json.JsonCodec
import ccf.transport._

class ServerEngineSpec extends Specification with Mockito with MockitoMatchers  {
  "ServerEngine" should {
    val codecMock = mock[Codec]
    codecMock.mimeType returns new MimeType("ccf/test")
    val sessionRequestMock = mock[SessionRequest]

    class TestServerEngine extends ServerEngine(codecMock) {
      override def createSessionRequest(transportRequest: TransportRequest) = {
        sessionRequestMock.transportRequest returns transportRequest
        sessionRequestMock
      }
    }

    val engine = new TestServerEngine

    "return codec's MIME type" in {
      engine.encodingMimeType must equalTo(codecMock.mimeType)
    }

    "process empty request as an error" in {
      codecMock.decodeRequest("") returns None
      engine.processRequest("") must throwAn[RuntimeException]
      there was one(codecMock).decodeRequest("")
    }

    "reply to non-empty request with empty success response" in {
      val testRequest = "test request"
      val testResponse = "test response"
      val transportRequestMock = mock[TransportRequest]
      codecMock.decodeRequest(testRequest) returns Some(transportRequestMock)
      val sessionResponseMock = mock[SessionResponse]
      sessionRequestMock.successResponse(None) returns sessionResponseMock
      val transportResponseMock = mock[TransportResponse]
      sessionResponseMock.transportResponse returns transportResponseMock
      codecMock.encodeResponse(transportResponseMock) returns testResponse

      engine.processRequest(testRequest) mustEqual testResponse
    }
  }

  "ServerEngine with ServerOperationInterceptor" should {
    val state = "this is the current state"
    class TestInterceptor extends DefaultServerOperationInterceptor {
      override def currentStateFor(channelId: ChannelId) = state
    }
    val engine = new ServerEngine(JsonCodec, new TestInterceptor)

    "reply with response containing Base64 encoded result for join request" in {
      val channelId = ChannelId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), ClientId.randomId, 0, Set())
      val joinRequest = JoinRequest(session, channelId)
      val response = engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
      val joinResponse: SessionResponse = SessionResponse(JsonCodec.decodeResponse(response).get, joinRequest)
      val Right(Success(_, Some(encodedResult: String))) = joinResponse.result
      state must equalTo(BASE64EncodingSerializer.deserialize(encodedResult))
    }
  }

  "ServerEngine with request blocking TransportRequestInterceptor" should {
    class BlockingInterceptor extends TransportRequestInterceptor {
      def isPermitted(r: TransportRequest) = false
    }
    val engine = new ServerEngine(JsonCodec, transportInterceptor = new BlockingInterceptor)

    "block a Join request" in {
      val channelId = ChannelId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), ClientId.randomId, 0, Set())
      val joinRequest = JoinRequest(session, channelId)
      val response = engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
      val joinResponse: SessionResponse = SessionResponse(JsonCodec.decodeResponse(response).get, joinRequest)
      val Left(Failure(_, reason)) = joinResponse.result
      reason must equalTo("Request not permitted")
    }
  }
}
