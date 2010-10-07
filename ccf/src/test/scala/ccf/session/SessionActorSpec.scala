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

import ccf.transport.{Connection, TransportResponse, ConnectionException}
import org.specs.Specification
import org.specs.mock.Mockito

class SessionActorSpec extends Specification with Mockito {
  val connection = mock[Connection]
  val clientId = ClientId.randomId
  val version = Version(1, 2)
  val channelId = ChannelId.randomId
  val testResult = Some("test result")
  val testReason = "test reason"

  "SessionActor on Join" should {
    val session = newSession(0, Set())
    val joinMessage = Message.Join(channelId)
    val joinRequest = JoinRequest(session, channelId)
    val sa = new SessionActor(connection, clientId, version)
    "reply with Success(...) when server returns valid response request" in {
      connection.send(joinRequest.transportRequest) returns Some(joinRequest.successResponse(None).transportResponse)
      sa !? joinMessage must equalTo(Right(Success(joinMessage, None)))
    }
    "reply with Success(...) when server returns valid response request with result" in {
      connection.send(joinRequest.transportRequest) returns Some(joinRequest.successResponse(testResult).transportResponse)
      sa !? joinMessage must equalTo(Right(Success(joinMessage, testResult)))
    }
    "reply with Success(...) when server returns no response to request" in {
      connection.send(joinRequest.transportRequest) returns None
      sa !? joinMessage must equalTo(Right(Success(joinMessage, None)))
    }
    "reply with Failure(...) and keep session state when server returns failure response to request" in {
      connection.send(joinRequest.transportRequest) returns Some(joinRequest.failureResponse(testReason).transportResponse)
      sa !? joinMessage must equalTo(Left(Failure(joinMessage, testReason)))
      val currentSession = (sa !? Shutdown).asInstanceOf[Session] must equalTo(session)
    }
    "update list of channels in session" in {
      connection.send(joinRequest.transportRequest) returns None
      sa !? joinMessage
      val currentSession = (sa !? Shutdown).asInstanceOf[Session]
      currentSession.seqId must equalTo(1)
      currentSession.channels must contain(channelId)
    }
  }

  "SessionActor on Part" should {
    val session = newSession(1, Set(channelId))
    val partMessage = Part(channelId)
    val partRequest = PartRequest(session, channelId)
    val sa = new SessionActor(connection, clientId, version, session)
    "reply with Success(...) when server returns valid response to request" in {
      connection.send(partRequest.transportRequest) returns Some(partRequest.successResponse(None).transportResponse)
      sa !? partMessage must equalTo(Right(Success(partMessage, None)))
    }
    "reply with Success(...) when server returns valid response to request with result" in {
      connection.send(partRequest.transportRequest) returns Some(partRequest.successResponse(testResult).transportResponse)
      sa !? partMessage must equalTo(Right(Success(partMessage, testResult)))
    }
    "reply with Success(...) when server returns no response to request" in {
      connection.send(partRequest.transportRequest) returns None
      sa !? partMessage must equalTo(Right(Success(partMessage, None)))
    }
    "update list of channels in session" in {
      connection.send(partRequest.transportRequest) returns None
      sa !? partMessage
      val currentSession = (sa !? Shutdown).asInstanceOf[Session]
      currentSession.channels must notContain(channelId)
    }
  }
  
  "SessionActor on connection failure" should {
    val session = newSession(0, Set())
    val message = Message.Join(channelId)
    "reply with Failure(...) when message send fails" in {
      val sa = new SessionActor(connection, clientId, version)
      doThrow(new ConnectionException("Error")).when(connection).send(JoinRequest(session, channelId).transportRequest)
      sa !? message must equalTo(Left(Failure(message, "ccf.transport.ConnectionException: Error")))
    }
  }

  "SessionActor on InChannelMsg" should {
    val session = newSession(1, Set(channelId))
    val content = Some(Map("a" -> "b", "c" -> Map("d" -> 3)))
    val requestType = "app/custom"
    val inChannelMessage = InChannelMessage(requestType, channelId, content)
    val inChannelRequest = InChannelRequest(session, requestType, channelId, content)
    val sa = new SessionActor(connection, clientId, version, session)
    "reply with Success(...) when server returns valid response to request" in {
      connection.send(inChannelRequest.transportRequest) returns Some(inChannelRequest.successResponse(None).transportResponse)
      sa !? inChannelMessage must equalTo(Right(Success(inChannelMessage, None)))
    }
    "reply with Success(...) when server returns valid response to request with result" in {
      connection.send(inChannelRequest.transportRequest) returns Some(inChannelRequest.successResponse(testResult).transportResponse)
      sa !? inChannelMessage must equalTo(Right(Success(inChannelMessage, testResult)))
    }
    "reply with Success(...) when server returns no response to request" in {
      connection.send(inChannelRequest.transportRequest) returns None
      sa !? inChannelMessage must equalTo(Right(Success(inChannelMessage, None)))
    }
  }

  private def newSession(seqId: Int, channels: Set[ChannelId]) = Session(connection, version, clientId, seqId, channels)
}
