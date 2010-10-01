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

import java.util.UUID
import org.specs.Specification
import org.specs.mock.Mockito

class MessageFactorySpec extends Specification with Mockito {
  val session = mock[Session]
  session.seqId returns 1
  session.version returns Version(2, 3)
  session.clientId returns ClientId(new UUID(4, 5))
  val channelId = new ChannelId(new UUID(6, 7))

  "MessageFactory#message" should {
    "create join message from join request" in {
      val request = JoinRequest(session, channelId)
      val expectedMsg = Message.Join(channelId)
      Message(request) mustEqual expectedMsg
    }

    "create part message from part request" in {
      val request = PartRequest(session, channelId)
      val expectedMsg = Message.Part(channelId)
      Message(request) mustEqual expectedMsg
    }

    "create in-channel message from in-channel request" in {
      val requestType = "test/requestType"
      val content = Some("test content")
      val request = InChannelRequest(session, requestType, channelId, content)
      val expectedMsg = Message.InChannel(requestType, channelId, content)
      Message(request) mustEqual expectedMsg
    }
  }
}
