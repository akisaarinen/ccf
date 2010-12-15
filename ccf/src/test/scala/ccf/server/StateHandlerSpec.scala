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
import org.specs.mock.Mockito
import ccf.{OperationSynchronizer, OperationSynchronizerFactory}
import ccf.session.{ChannelId, ClientId}
import ccf.messaging.Message

class StateHandlerSpec extends Specification with Mockito {
  val stateHandler = new StateHandler(new OperationSynchronizerFactory {
    def createSynchronizer = mock[OperationSynchronizer]
  })
  val channel = ChannelId.randomId
  val clientA = ClientId.randomId
  val clientB = ClientId.randomId
  
  "State handler" should {
    "list clients for channel" in {
      stateHandler.join(clientA, channel)
      stateHandler.join(clientB, channel)
      stateHandler.clientsForChannel(channel) must haveTheSameElementsAs(List(clientA, clientB))
    }
    "list no clients if none on channel" in {
      stateHandler.clientsForChannel(channel) must equalTo(List())
    }
    "list other clients for channel" in {
      stateHandler.join(clientA, channel)
      stateHandler.join(clientB, channel)
      stateHandler.otherClientsFor(clientA) must equalTo(List(clientB))
    }
    "list no clients as other clients if only client on channel" in {
      stateHandler.join(clientA, channel)
      stateHandler.otherClientsFor(clientA) must equalTo(List())
    }
    "remove client's messages before joining channel" in {
      stateHandler.addMsg(clientA, channel, mock[Message])
      stateHandler.addMsg(clientA, channel, mock[Message])
      stateHandler.join(clientA, channel)
      stateHandler.getMsgs(clientA, channel) must equalTo(List())
    }
  }
}