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
import org.mockito.Matchers.anyString
import javax.activation.MimeType
import ccf.session._
import ccf.transport.json.JsonCodec
import ccf.transport._
import ccf.tree.indexing.TreeIndex
import ccf.messaging.{ChannelShutdown, OperationContext}
import ccf.tree.operation._

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

  "ServerEngine with interceptors" should {
    val state = "this is the current state"
    val notifier = mock[NotifyingInterceptor]
    class TestInterceptor extends DefaultServerOperationInterceptor {
      override def currentStateFor(channelId: ChannelId) = state
    }
    val engine = new ServerEngine(JsonCodec, operationInterceptor = new TestInterceptor, notifyingInterceptor = Some(notifier))

    "reply with response containing Base64 encoded result for join request" in {
      val channelId = ChannelId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), ClientId.randomId, 0, Set())
      val joinRequest = JoinRequest(session, channelId)
      val response = engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
      val joinResponse: SessionResponse = SessionResponse(JsonCodec.decodeResponse(response).get, joinRequest)
      val Right(Success(_, Some(encodedResult: String))) = joinResponse.result
      state must equalTo(BASE64EncodingSerializer.deserialize(encodedResult))
    }

    "reply with response containing result for part request" in {
      val channelId = ChannelId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), ClientId.randomId, 0, Set(channelId))
      val partRequest = PartRequest(session, channelId)
      val response = engine.processRequest(JsonCodec.encodeRequest(partRequest.transportRequest))
      val partResponse: SessionResponse = SessionResponse(JsonCodec.decodeResponse(response).get, partRequest)
      partResponse.result must equalTo(Right(Success(partRequest, None)))
    }

    "have an intialized state after joining" in {
      val channelId = ChannelId.randomId
      val clientId = ClientId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), clientId, 0, Set())
      val joinRequest = JoinRequest(session, channelId)
      engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
      engine.stateHandler.clientStateOption(clientId) must beSome[ClientState]
    }

    "not have an initialized state after parting" in {
      val channelId = ChannelId.randomId
      val clientId = ClientId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), clientId, 0, Set())
      val joinRequest = JoinRequest(session, channelId)
      engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
      engine.stateHandler.clientStateOption(clientId) must beSome[ClientState]
      val partRequest = PartRequest(session, channelId)
      engine.processRequest(JsonCodec.encodeRequest(partRequest.transportRequest))
      engine.stateHandler.clientStateOption(clientId) must beNone
    }

    "reply with success result to operation request and notify other clients" in {
      val channelId = ChannelId.randomId
      val clientId = ClientId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), clientId, 0, Set())
      val joinRequest = JoinRequest(session, channelId)
      engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
      val operationRequest = OperationContextRequest(session, channelId, new OperationContext(NoOperation(), 0, 0))
      val response = engine.processRequest(JsonCodec.encodeRequest(operationRequest.transportRequest))
      val operationResponse = SessionResponse(JsonCodec.decodeResponse(response).get, operationRequest)
      operationResponse.result must equalTo(Right(Success(operationRequest, None)))
      there was one(notifier).notify(clientId, channelId)
    }

    "reply with failure result (client state not initialized) on operation and no clientState initialized on server (after reboot)" in {
      val channelId = ChannelId.randomId
      val clientId = ClientId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), clientId, 0, Set())
      val operationRequest = OperationContextRequest(session, channelId, new OperationContext(NoOperation(), 0, 0))
      val response = engine.processRequest(JsonCodec.encodeRequest(operationRequest.transportRequest))
      val Left(Failure(_, errorMessage)) = SessionResponse(JsonCodec.decodeResponse(response).get, operationRequest).result
      val expectedErrorMessage = "operation request handling error: java.lang.Exception: Server has no client state initialized for client, possibly due to reboot"
      errorMessage must startWith(expectedErrorMessage)
    }

    "reply with success result containing list of encoded msgs to inchannel request" in {
      val channelId = ChannelId.randomId
      val clientId = ClientId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), clientId, 0, Set())
      val joinRequest = JoinRequest(session, channelId)
      engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
      val msg1 = OperationContext(DeleteOperation(TreeIndex(0)), 0, 0)
      val msg2 = OperationContext(MoveOperation(TreeIndex(0), TreeIndex(1)), 1, 0)
      engine.stateHandler.addMsg(clientId, channelId, msg1)
      engine.stateHandler.addMsg(clientId, channelId, msg2)
      val inChannelRequest = InChannelRequest(session, "getMsgs", channelId, None)
      val response = engine.processRequest(JsonCodec.encodeRequest(inChannelRequest.transportRequest))
      val Right(Success(_, Some(encodedMsgList: List[_]))) = SessionResponse(JsonCodec.decodeResponse(response).get, inChannelRequest).result
      List(msg1.encode, msg2.encode) must equalTo(encodedMsgList)
    }

    "reply with success result containing list of unread encoded msgs to inchannel request" in {
      val channelId = ChannelId.randomId
      val clientId = ClientId.randomId
      val session = new Session(mock[Connection], ccf.session.Version(1, 2), clientId, 0, Set())
      val joinRequest = JoinRequest(session, channelId)
      engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
      val msg1 = OperationContext(DeleteOperation(TreeIndex(0)), 0, 0)
      val msg2 = OperationContext(MoveOperation(TreeIndex(0), TreeIndex(1)), 1, 0)
      val msg3 = OperationContext(MoveOperation(TreeIndex(0), TreeIndex(1)), 2, 0)
      engine.stateHandler.addMsg(clientId, channelId, msg1)
      engine.stateHandler.addMsg(clientId, channelId, msg2)
      engine.stateHandler.addMsg(clientId, channelId, msg3)
      val inChannelRequest = InChannelRequest(session, "getMsgs", channelId, Some(1))
      val response = engine.processRequest(JsonCodec.encodeRequest(inChannelRequest.transportRequest))
      val Right(Success(_, Some(encodedMsgList: List[_]))) = SessionResponse(JsonCodec.decodeResponse(response).get, inChannelRequest).result
      List(msg2.encode, msg3.encode) must equalTo(encodedMsgList)
    }
  }

  "ServerEngine with mocked OperationPersistor" should {
   "pass serverEngine to operationPersistor#applyOperation when processing operation" in {
    val operationInterceptorMock = mock[ServerOperationInterceptor]
    val interceptor = new DefaultServerOperationInterceptor {
      override def applyOperation(shutdownListener: ShutdownListener, channelId: ChannelId, op: TreeOperation) {
        operationInterceptorMock.applyOperation(shutdownListener, channelId, op)
      }
    }
    val engine = new ServerEngine(JsonCodec, interceptor)
    val channelId = ChannelId.randomId
    val clientId = ClientId.randomId
    val session = new Session(mock[Connection], ccf.session.Version(1, 2), clientId, 0, Set())
    val joinRequest = JoinRequest(session, channelId)
    engine.processRequest(JsonCodec.encodeRequest(joinRequest.transportRequest))
    val operationRequest = OperationContextRequest(session, channelId, new OperationContext(NoOperation(), 0, 0))
    engine.processRequest(JsonCodec.encodeRequest(operationRequest.transportRequest))
    there was one(operationInterceptorMock).applyOperation(engine, channelId, NoOperation())
   }
  }

  "ServerEngine with request blocking TransportRequestInterceptor" should {
    class BlockingInterceptor extends TransportRequestInterceptor {
      def isPermitted(r: TransportRequest) = (false, "Request not permitted")
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

  "ServerEngine on shutdown" should {
    val channel = ChannelId.randomId
    val client1 = ClientId.randomId
    val client2 = ClientId.randomId
    val otherChannel = ChannelId.randomId
    val clientInOtherChannel = ClientId.randomId
    val serverEngine = new ServerEngine(JsonCodec)
    doBefore {
      serverEngine.stateHandler.join(client1, channel)
      serverEngine.stateHandler.join(client2, channel)
      serverEngine.stateHandler.join(clientInOtherChannel, otherChannel)
    }

    "remove clients from channel" in {
      serverEngine.shutdown(channel, "any reason")
      serverEngine.stateHandler.clientsForChannel(channel) must equalTo(List())
      serverEngine.stateHandler.clientsForChannel(otherChannel) must equalTo(List(clientInOtherChannel))
    }
    "inform clients on channel of shutdown" in {
      serverEngine.shutdown(channel, "any reason")
      serverEngine.stateHandler.getMsgs(client1, channel) must equalTo(List(ChannelShutdown("any reason")))
      serverEngine.stateHandler.getMsgs(client2, channel) must equalTo(List(ChannelShutdown("any reason")))
      serverEngine.stateHandler.getMsgs(clientInOtherChannel, otherChannel) must equalTo(List())
    }
  }
}
