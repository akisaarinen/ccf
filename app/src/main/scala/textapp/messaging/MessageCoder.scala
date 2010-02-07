package textapp.messaging

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import ccf.messaging._
import scala.util.matching.Regex

class MessageCoder {
  private val operationCoder = new OperationCoder
  def encode(msg: ConcurrentOperationMessage[TreeOperation]): String = {
    val encodedOp = operationCoder.encode(msg.op)
    "%s,%d,%d".format(encodedOp, msg.localMessage, msg.expectedRemoteMessage)
  }

  def decode(s: String): ConcurrentOperationMessage[TreeOperation] = {
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
