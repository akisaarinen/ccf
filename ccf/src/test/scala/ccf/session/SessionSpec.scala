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

object SessionSpec extends Specification with Mockito {
  val connection = mock[Connection]
  val version = Version(1, 2)
  val clientId = ClientId.randomId
  val existingChannelId = ChannelId.randomId
  val newChannelId = ChannelId.randomId
  val channels = Set(existingChannelId)
  val session = Session(connection, version, clientId, 0, channels)
  "Session next(...)" should {
    "produce new session with incremented seqId" in {
      session.next(Set()).seqId must equalTo(1)
    }
    "produce new session with updated channel list" in {
      session.next(Set(newChannelId)).channels must equalTo(Set(newChannelId))
    }
  }
  "Session send(...)" should {
    "send valid Join message, sending correct TransportRequest and producing correct Session" in {
      val joinRequest = JoinRequest(session, newChannelId).transportRequest
      val joinMessage = Join(newChannelId)
      connection.send(joinRequest) returns None
      val (nextSession, result) = session.send(joinMessage)

      result must equalTo(Right(Success(joinMessage, None)))
      nextSession.seqId must equalTo(1)
      nextSession.channels must equalTo(Set(existingChannelId, newChannelId))
      there was one(connection).send(joinRequest)
    }
    "send Part message, sending correct TransportRequest and producing correct Session" in {
      val partRequest = PartRequest(session, existingChannelId).transportRequest
      val partMessage = Part(existingChannelId)
      connection.send(partRequest) returns None
      val (nextSession, result) = session.send(partMessage)

      result must equalTo(Right(Success(partMessage, None)))
      nextSession.seqId must equalTo(1)
      nextSession.channels must equalTo(Set())
      there was one(connection).send(partRequest)
    }
    "report failure and keep current session state, if transport layer fails with ConnectException" in {
      val request = JoinRequest(session, newChannelId).transportRequest
      val message = Join(newChannelId)
      doThrow(new ConnectionException("Error")).when(connection).send(request)
      val (nextSession, result) = session.send(message)
      nextSession must equalTo(session)
      result must equalTo(Left(Failure(message, "ccf.transport.ConnectionException: Error")))
    }
  }
}
