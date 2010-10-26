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

import ccf.session.{ChannelId, ClientId}
import ccf.messaging.{OperationContext, Message}

class PendingMessageBuffer {
  case class PendingMsg(clientId: ClientId, channelId: ChannelId, msg: Message)
  private var msgs: List[PendingMsg] = List()

  def add(clientId: ClientId, channelId: ChannelId, msg: Message) {
    msgs = msgs ::: List(PendingMsg(clientId, channelId, msg))
  }

  def get(clientId: ClientId, channelId: ChannelId): List[Message] = {
    val (msgsForClient, other) = msgs.partition {m => m.clientId == clientId && m.channelId == channelId}
    msgsForClient.map(_.msg)
  }

  def remove(clientId: ClientId, channelId: ChannelId) {
    msgs = msgs.filter {m => m.clientId != clientId || m.channelId != channelId}
  }

  def removeReceivedMessages(clientId: ClientId, channelId: ChannelId, nextMessage: Int) {
    msgs = msgs.filter {m => m.clientId != clientId || notReceivedMessage(m, nextMessage)}
  }

  private def notReceivedMessage(m: PendingMsg, nextMessage: Int): Boolean = {
    m.msg match {
      case cop: OperationContext => (cop.localMsgSeqNo>= nextMessage)
      case _ => true
    }
  }
}