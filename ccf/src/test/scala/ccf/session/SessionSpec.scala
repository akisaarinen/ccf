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
import ccf.transport.{Connection, ConnectionException}
import ccf.OperationContext

object SessionSpec extends Specification with Mockito {
  val connection = mock[Connection]
  val version = Version(1, 2)
  val clientId = ClientId.randomId
  val existingChannelId = ChannelId.randomId
  val newChannelId = ChannelId.randomId
  val channels = Set(existingChannelId)
  val session = Session(connection, version, clientId, 0, channels)
  val testResult = Some("test result")
  val testReason = "test reason"
  "Session next(...)" should {
    "produce new session with incremented seqId" in {
      session.next(Set()).seqId must equalTo(1)
    }
    "produce new session with updated channel list" in {
      session.next(Set(newChannelId)).channels must equalTo(Set(newChannelId))
    }
  }
  "Session send(...)" should {
    "with any message" in {
      val msg = mock[Message]
      val mockReq = mock[SessionRequest]
      val mockRes = mock[SessionResponse]
      val newSession = session.copy(seqId = session.seqId+1)

      "Return success and new session on empty response" in {
        msg.send(session) returns((newSession, None))

        val (nextSession, result) = session.send(msg)
        there was one(msg).send(session)
        result must equalTo(Right(Success(msg, None)))
        nextSession must equalTo(newSession)
      }
      "Return success and new session on success response without result" in {
        mockRes.result returns Left(Success(mockReq, None))
        msg.send(session) returns((newSession, Some(mockRes)))

        val (nextSession, result) = session.send(msg)
        there was one(msg).send(session)
        result must equalTo(Right(Success(msg, None)))
        nextSession must equalTo(newSession)
      }
      "Return success and new session on success response with result" in {
        mockRes.result returns Left(Success(mockReq, testResult))
        msg.send(session) returns((newSession, Some(mockRes)))

        val (nextSession, result) = session.send(msg)
        there was one(msg).send(session)
        result must equalTo(Right(Success(msg, testResult)))
        nextSession must equalTo(newSession)
      }
      "Return failure and retain session state on failure response" in {
        mockRes.result returns Right(Failure(mockReq, testReason))
        msg.send(session) returns((newSession, Some(mockRes)))

        val (nextSession, result) = session.send(msg)
        there was one(msg).send(session)
        result must equalTo(Left(Failure(msg, testReason)))
        nextSession must equalTo(session)
      }
      "Return failure and retain session state on exception in Message#send" in {
        val ex = new RuntimeException(testReason)
        msg.send(session) throws ex

        val (nextSession, result) = session.send(msg)
        there was one(msg).send(session)
        result must equalTo(Left(Failure(msg, ex.toString)))
        nextSession must equalTo(session)
      }
    }
    "with Join message" in {
      val joinRequest = JoinRequest(session, newChannelId)
      val joinMessage = Join(newChannelId)

      "send valid request, sending correct TransportRequest and producing correct Session" in {
        connection.send(joinRequest.transportRequest) returns None
        val (nextSession, result) = session.send(joinMessage)

        result must equalTo(Right(Success(joinMessage, None)))
        nextSession.seqId must equalTo(1)
        nextSession.channels must equalTo(Set(existingChannelId, newChannelId))
        there was one(connection).send(joinRequest.transportRequest)
      }
    }
    "with Part message" in {
      val partRequest = PartRequest(session, existingChannelId)
      val partMessage = Part(existingChannelId)

      "send valid request, sending correct TransportRequest and producing correct Session" in {
        connection.send(partRequest.transportRequest) returns None
        val (nextSession, result) = session.send(partMessage)

        result must equalTo(Right(Success(partMessage, None)))
        nextSession.seqId must equalTo(1)
        nextSession.channels must equalTo(Set())
        there was one(connection).send(partRequest.transportRequest)
      }
    }
    "with InChannel message" in {
      val content = Some("test content")
      val requestType = "test/type"
      val inChannelRequest = InChannelRequest(session, requestType, existingChannelId, content)
      val inChannelMessage = InChannelMessage(requestType, existingChannelId, content)

      "send valid request, sending correct TransportRequest and producing correct Session" in {
        connection.send(inChannelRequest.transportRequest) returns None
        val (nextSession, result) = session.send(inChannelMessage)

        result must equalTo(Right(Success(inChannelMessage, None)))
        nextSession.seqId must equalTo(1)
        nextSession.channels must equalTo(Set(existingChannelId))
        there was one(connection).send(inChannelRequest.transportRequest)
      }
    }
    "with OperationContextMessage message" in {
      val context = mock[OperationContext]
      val operationContextRequest = OperationContextRequest(session, existingChannelId, context)
      val operationContextMessage = OperationContextMessage(existingChannelId, context)

      "send valid request, sending correct TransportRequest and producing correct Session" in {
        connection.send(operationContextRequest.transportRequest) returns None
        val (nextSession, result) = session.send(operationContextMessage)

        result must equalTo(Right(Success(operationContextMessage, None)))
        nextSession.seqId must equalTo(1)
        nextSession.channels must equalTo(Set(existingChannelId))
        there was one(connection).send(operationContextRequest.transportRequest)
      }
    }
  }
}
