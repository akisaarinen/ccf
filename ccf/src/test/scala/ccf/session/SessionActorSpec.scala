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

  "SessionActor on Join" should {
    val session = newSession(0, Set())
    val joinMessage = Join(channelId)
    val joinRequest = JoinRequest(session, channelId).transportRequest
    val sa = new SessionActor(connection, clientId, version)
    "reply with Success(...) when server returns valid response request" in {
      connection.send(joinRequest) returns Some(TransportResponse(joinRequest.headers, None))
      sa !? joinMessage must equalTo(Right(Success(joinMessage, None)))
    }
    "reply with Success(...) when server returns no response to request" in {
      connection.send(joinRequest) returns None
      sa !? joinMessage must equalTo(Right(Success(joinMessage, None)))
    }
    "update list of channels in session" in {
      connection.send(joinRequest) returns None
      sa !? joinMessage
      val currentSession = (sa !? Shutdown).asInstanceOf[Session]
      currentSession.seqId must equalTo(1)
      currentSession.channels must contain(channelId)
    }
  }

  "SessionActor on Part" should {
    val session = newSession(1, Set(channelId))
    val partMessage = Part(channelId)
    val partRequest = PartRequest(session, channelId).transportRequest
    val sa = new SessionActor(connection, clientId, version, session)
    "reply with Success(...) when server returns valid response to request" in {
      connection.send(partRequest) returns Some(TransportResponse(partRequest.headers, None))
      sa !? partMessage must equalTo(Right(Success(partMessage, None)))
    }
    "reply with Success(...) when server returns no response to request" in {
      connection.send(partRequest) returns None
      sa !? partMessage must equalTo(Right(Success(partMessage, None)))
    }
    "update list of channels in session" in {
      connection.send(partRequest) returns None
      sa !? partMessage
      val currentSession = (sa !? Shutdown).asInstanceOf[Session]
      currentSession.channels must notContain(channelId)
    }
  }
  
  "SessionActor on connection failure" should {
    val session = newSession(0, Set())
    val message = Join(channelId)
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
    "reply with Success(...) when server returns valid response to request" in {
      val sa = new SessionActor(connection, clientId, version, session)
      connection.send(inChannelRequest.transportRequest) returns None
      sa !? inChannelMessage must equalTo(Right(Success(inChannelMessage, None)))
    }
  }

  private def newSession(seqId: Int, channels: Set[ChannelId]) = Session(connection, version, clientId, seqId, channels)
}
