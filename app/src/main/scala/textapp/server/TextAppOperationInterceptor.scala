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

package textapp.server

import ccf.tree.operation.TreeOperation
import ccf.server.{ServerOperationInterceptor, Server}
import ccf.transport.{ClientId, ChannelId}
import textapp.TextDocument

class TextAppOperationInterceptor(document: TextDocument) extends ServerOperationInterceptor {
  override def currentStateFor(channelId: ChannelId): Any = {
    document
  }
  override def applyOperation(server: Server, clientId: ClientId, channelId: ChannelId, op: TreeOperation): Unit = {
    document.applyOp(op)
  }
  override def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
  override def operationsForAllClients(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
}
