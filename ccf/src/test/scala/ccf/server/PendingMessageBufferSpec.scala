package ccf.server

import org.specs.Specification
import ccf.session.{ClientId, ChannelId}
import ccf.transport.Event.Msg
import ccf.messaging.OperationContext
import ccf.tree.operation.NoOperation

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

class PendingMessageBufferSpec extends Specification {
  val buffer = new PendingMessageBuffer
  val clientA = ClientId.randomId
  val clientB = ClientId.randomId
  val channelA = ChannelId.randomId
  val channelB = ChannelId.randomId
  val msgA = OperationContext(NoOperation(), 0, 0)
  val msgB = OperationContext(NoOperation(), 1, 1)
  val msgC = OperationContext(NoOperation(), 2, 2)


  "Message buffer" should {
    "return list of messages for client on given channel" in {
      buffer.add(clientA, channelA, msgA)
      buffer.add(clientA, channelA, msgB)
      buffer.get(clientA, channelA) must equalTo(List(msgA, msgB))
    }
    "remove all clients messages on a channel" in {
      buffer.add(clientA, channelA, msgA)
      buffer.add(clientA, channelA, msgB)
      buffer.add(clientA, channelA, msgC)
      buffer.remove(clientA, channelA)
      buffer.get(clientA, channelA) must equalTo(List())
    }
    "remove old messages" in {
      buffer.add(clientA, channelA, msgA)
      buffer.add(clientA, channelA, msgB)
      buffer.add(clientA, channelA, msgC)
      buffer.removeReceivedMessages(clientA, channelA, 1)
      buffer.get(clientA, channelA) must equalTo(List(msgB, msgC))
    }
    "get all messages and remove old" in {
      buffer.add(clientA, channelA, msgA)
      buffer.add(clientA, channelA, msgB)
      buffer.add(clientA, channelA, msgC)
      buffer.get(clientA, channelA, 1) must equalTo(List(msgB, msgC))
    }
  }
}