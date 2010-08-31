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

import ccf.messaging.Message
import ccf.server.Server
import ccf.transport.Event
import ccf.transport.{ClientId, ChannelId}
import ccf.tree.JupiterTreeTransformation
import ccf.tree.operation.TreeOperation
import ccf.JupiterOperationSynchronizerFactory
import textapp.TextDocument

class DocumentHandler {
  private val document = new TextDocument("")
  private var messages = List[Event.Msg]()
    
  private val factory = new JupiterOperationSynchronizerFactory(true, JupiterTreeTransformation)
  private val interceptor = new TextAppOperationInterceptor(document)
  private val transport = new TextAppTransportActor(onMessageToClient)
  private val server = new Server(factory, interceptor, transport)

  private def onMessageToClient(msg: Event.Msg): Unit = messages.synchronized {
    messages = messages ::: List(msg)
  }

  def onJoin(clientId: ClientId, channelId: ChannelId): TextDocument = {
    server !? Event.Join(clientId, channelId) match {
      case Event.State(_, _, state) => state.asInstanceOf[TextDocument]
      case _ => error("Join failed")
    }
  }

  def onQuit(clientId: ClientId, channelId: ChannelId) {
    server !? Event.Quit(clientId, channelId)
  }
  
  def onMsg(clientId: ClientId, channelId: ChannelId, msg: Message) {
    server !? Event.Msg(clientId, channelId, msg)
  }

  def getMessages(clientId: ClientId): (List[Message], TextDocument) = messages.synchronized {
    def isForClient(msg: Event.Msg) = msg match {
      case Event.Msg(id, _, _) if (id == clientId) => true
      case _ => false
    }

    val msgsForClient = messages.filter(isForClient(_))
    val msgsNotForClient = messages.filter(!isForClient(_))
    messages = msgsNotForClient
    (msgsForClient.map(_.msg), document)
  }
}
