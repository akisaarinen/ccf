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

package ccf.messaging

import ccf.tree.operation.TreeOperation
import ccf.tree.operation.TreeOperationDecoder
import ccf.session.OperationDecoder

sealed abstract class Message {
  def encode: Any
}

object Message {
  def apply(map: Map[String, Any], operationDecoder: OperationDecoder): Message = {
    val messageType = map("type")
    messageType match {
      case "operation" => OperationContext(map, operationDecoder)
      case "error" => ErrorMessage(map)
      case "shutdown" => ChannelShutdown(map)
      case _ => throw new IllegalArgumentException("Unrecognized message type")
    }
  }
}

case class OperationContext(val op: TreeOperation, val localMsgSeqNo: Int, val remoteMsgSeqNo: Int) extends Message {
  def encode: Any = Map("type" -> "operation", "op" -> op.encode, "localMsgSeqNo" -> localMsgSeqNo, "remoteMsgSeqNo" -> remoteMsgSeqNo)
}
object OperationContext {
  def apply(map: Map[String, Any], operationDecoder: OperationDecoder): OperationContext = {
    val op = operationDecoder.decode(map("op"))
    val localMsg = map("localMsgSeqNo").asInstanceOf[Int]
    val remoteMsg = map("remoteMsgSeqNo").asInstanceOf[Int]
    OperationContext(op, localMsg, remoteMsg)
  }
}
case class ErrorMessage(val reason: String) extends Message {
  def encode = Map("type" -> "error", "reason" -> reason)
}

object ErrorMessage {
  def apply(map: Map[String, Any]): ErrorMessage = {
    val reason: String = map("reason").asInstanceOf[String]
    ErrorMessage(reason)
  }
}

case class ChannelShutdown(val reason: String) extends Message {
  def encode = Map("type" -> "shutdown", "reason" -> reason)
}

object ChannelShutdown {
  def apply(map: Map[String, Any]): ChannelShutdown = {
    val reason: String = map("reason").asInstanceOf[String]
    ChannelShutdown(reason)
  }
}
