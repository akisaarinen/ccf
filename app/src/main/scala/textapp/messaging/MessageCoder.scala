package textapp.messaging

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import ccf.messaging._
import scala.util.matching.Regex

class MessageCoder {
  protected val operationCoder = new OperationCoder
  def encode(msg: Message[TreeOperation]): String = msg match {
    case msg : ConcurrentOperationMessage[TreeOperation] =>
      val encodedOp = operationCoder.encode(msg.op)
      "%s,%d,%d".format(encodedOp, msg.localMessage, msg.expectedRemoteMessage)
    case _ =>
      error("Unknown message type, unable to encode")
  }

  def decode(s: String): Message[TreeOperation] = {
    val MsgExpr = new Regex("""(.*),(\d+),(\d+)""")
    s match {
      case MsgExpr(encodedOp, localMessage, expectedRemoteMessage) => {
        val op = operationCoder.decode(encodedOp)
        ConcurrentOperationMessage[TreeOperation](op, localMessage.toInt, expectedRemoteMessage.toInt)
      }
      case _ => error("no decoding for message string '%s' available".format(s))
    }
  }
}
