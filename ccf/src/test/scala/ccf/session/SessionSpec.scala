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

  "Session#next" should {
    "produce new session with incremented seqId" in {
      session.next(Set()).seqId must equalTo(1)
    }
    
    "produce new session with updated channel list" in {
      session.next(Set(newChannelId)).channels must equalTo(Set(newChannelId))
    }
  }

  "Session#send with any message" should {
    val msg = mock[Message]
    val mockReq = mock[SessionRequest]
    val mockRes = mock[SessionResponse]
    val newSession = session.copy(seqId = session.seqId+1)

    "return success and new session on empty response" in {
      msg.send(session) returns((newSession, None))

      val (nextSession, result) = session.send(msg)
      there was one(msg).send(session)
      result must equalTo(Right(Success(msg, None)))
      nextSession must equalTo(newSession)
    }

    "return success and new session on success response without result" in {
      mockRes.result returns Right(Success(mockReq, None))
      msg.send(session) returns((newSession, Some(mockRes)))

      val (nextSession, result) = session.send(msg)
      there was one(msg).send(session)
      result must equalTo(Right(Success(msg, None)))
      nextSession must equalTo(newSession)
    }

    "return success and new session on success response with result" in {
      mockRes.result returns Right(Success(mockReq, testResult))
      msg.send(session) returns((newSession, Some(mockRes)))

      val (nextSession, result) = session.send(msg)
      there was one(msg).send(session)
      result must equalTo(Right(Success(msg, testResult)))
      nextSession must equalTo(newSession)
    }

    "return failure and retain session state on failure response" in {
      mockRes.result returns Left(Failure(mockReq, testReason))
      msg.send(session) returns((newSession, Some(mockRes)))

      val (nextSession, result) = session.send(msg)
      there was one(msg).send(session)
      result must equalTo(Left(Failure(msg, testReason)))
      nextSession must equalTo(session)
    }

    "return failure and retain session state on exception in Message#send" in {
      val ex = new RuntimeException(testReason)
      msg.send(session) throws ex

      val (nextSession, result) = session.send(msg)
      there was one(msg).send(session)
      result must equalTo(Left(Failure(msg, ex.toString)))
      nextSession must equalTo(session)
    }
  }

  "Session#send with join message" should {
    val joinRequest = JoinRequest(session, newChannelId)
    val joinMessage = Message.Join(newChannelId)
    val expectedSession = session.next(session.channels + newChannelId)

    sendMessageSpec(joinMessage, joinRequest, expectedSession)
  }

  "Session#send with part message" should {
    val partRequest = PartRequest(session, existingChannelId)
    val partMessage = Message.Part(existingChannelId)
    val expectedSession = session.next(session.channels - existingChannelId)

    sendMessageSpec(partMessage, partRequest, expectedSession)
  }

  "Session#send with in-channel message" should {
    val content = Some("test content")
    val requestType = "test/type"
    val inChannelRequest = InChannelRequest(session, requestType, existingChannelId, content)
    val inChannelMessage = InChannelMessage(requestType, existingChannelId, content)
    val expectedSession = session.next(session.channels)

    sendMessageSpec(inChannelMessage, inChannelRequest, expectedSession)
  }

  "Session#send with operation context message" should {
    val context = mock[OperationContext]
    val operationContextRequest = OperationContextRequest(session, existingChannelId, context)
    val operationContextMessage = OperationContextMessage(existingChannelId, context)
    val expectedSession = session.next(session.channels)

    sendMessageSpec(operationContextMessage, operationContextRequest, expectedSession)
  }

  def sendMessageSpec(msg: Message, expectedRequest: SessionRequest, expectedSession: Session) {
    "send correct request and produce correct session" in {
      connection.send(expectedRequest.transportRequest) returns None
      val (nextSession, result) = session.send(msg)

      there was one(connection).send(expectedRequest.transportRequest)
      result must equalTo(Right(Success(msg, None)))
      nextSession must equalTo(expectedSession)
    }
  }
}
