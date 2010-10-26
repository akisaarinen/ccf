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

import ccf.session.{ClientId, ChannelId}
import ccf.OperationSynchronizerFactory
import collection.mutable.HashMap

class StateHandler(factory: OperationSynchronizerFactory) {
  private val clientStates = new HashMap[ClientId, ClientState]
  private val pendingMsgs = new PendingMessageBuffer

  def join(clientId: ClientId, channelId: ChannelId) = {
    val synchronizer = factory.createSynchronizer
    clientStates(clientId) = new ClientState(channelId, synchronizer)
  }

  def part(clientId: ClientId, channelId: ChannelId) = {
    clientStates -= clientId
  }

  def clientState(clientId: ClientId): Option[ClientState] = {
    clientStates.get(clientId)
  }
}