package ccf.client

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

import ccf.JupiterOperationSynchronizer
import ccf.tree.JupiterTreeTransformation
import ccf.messaging.OperationContext
import ccf.transport.http.HttpConnection
import ccf.session._
import java.util.UUID
import ccf.tree.operation.TreeOperation
import java.net.URL
import ccf.transport.BASE64EncodingSerializer

class Client[Document](url: URL, version: Version, decoder: OperationDecoder, clientId: ClientId = ClientId.randomId) {
  protected val connection = HttpConnection.create(url)
  protected val sa = new SessionActor(connection, clientId, version)
  protected val serializer = BASE64EncodingSerializer
  protected val clientSync = new JupiterOperationSynchronizer(false, JupiterTreeTransformation)

  def join(channel: ChannelId): Document = {
    sa !? Message.Join(channel) match {
      case Right(Success(_, Some(data))) => serializer.deserialize[Document](data.toString)
      case Left(Failure(_, reason)) => error("Error joining " + channel + ": " + reason)
      case m => error("Unknown reply to join: " + m)
    }
  }

  def part(channel: ChannelId) {
    sa !? Message.Part(channel) match {
      case Right(Success(_, _)) =>
      case Left(Failure(_, reason)) => error("Error parting from " + channel + ": " + reason)
      case m => error("Unknown reply to part: " + m)
    }
  }

  def shutdown: Session = {
    sa !? Message.Shutdown match {
      case Left(Failure(_, reason)) => error("Error shutting down: " + reason)
      case m => m.asInstanceOf[Session]
    }
  }

  def getOperations(channel: ChannelId): List[TreeOperation] = {
    (sa !? Message.InChannel("channel/getMsgs", channel, Some(0))) match {
      case Right(Success(_, Some(encodedMessages: List[_]))) => {
        val messageMaps: List[Map[String, String]] = encodedMessages.asInstanceOf[List[Map[String, String]]]
        val messages = messageMaps.map(ccf.messaging.Message(_, decoder))
        messages.map { msg => clientSync.receiveRemoteOperation(msg.asInstanceOf[OperationContext]) }
      }
      case Right(Success(_, None)) => List()
      case Left(Failure(_, reason)) => error("Error getting operations for " + channel + ": " + reason)
      case m => error("Unknown reply to getting operations: " + m)
    }
  }

  def sendOperation(channel: ChannelId, op: TreeOperation) {
    val context = clientSync.createLocalOperation(op)
    sa !? Message.OperationContext(channel, context) match {
      case Right(Success(_, _)) =>
      case Left(Failure(_, reason)) => error("Error sending operation for " + channel + ": " + reason)
      case m => error("Unknown reply to sending operation: " + m)
    }
  }
}