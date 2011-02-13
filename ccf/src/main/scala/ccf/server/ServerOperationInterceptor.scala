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

import java.io.Serializable
import ccf.session.{ClientId, ChannelId}
import ccf.tree.operation.TreeOperation

trait ServerOperationInterceptor {
  def currentStateFor(channelId: ChannelId): Serializable
  def applyOperation(shutdownListener: ShutdownListener, channelId: ChannelId, op: TreeOperation): Unit
  def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
  def operationsForAllClients(channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
}