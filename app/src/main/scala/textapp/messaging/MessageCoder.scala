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

package textapp.messaging

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import ccf.messaging._
import scala.util.matching.Regex

class MessageCoder {
  protected val operationCoder = new OperationCoder
  def encode(msg: Message): String = msg match {
    case msg : ConcurrentOperationMessage =>
      val encodedOp = operationCoder.encode(msg.op)
      "%s,%d,%d".format(encodedOp, msg.localMessage, msg.expectedRemoteMessage)
    case _ =>
      error("Unknown message type, unable to encode")
  }

  def decode(s: String): Message = {
    val MsgExpr = new Regex("""(.*),(\d+),(\d+)""")
    s match {
      case MsgExpr(encodedOp, localMessage, expectedRemoteMessage) => {
        val op = operationCoder.decode(encodedOp)
        ConcurrentOperationMessage(op, localMessage.toInt, expectedRemoteMessage.toInt)
      }
      case _ => error("no decoding for message string '%s' available".format(s))
    }
  }
}
