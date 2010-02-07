package textapp.messaging

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import scala.util.matching.Regex
import textapp.Elem

object OperationCoding {
  def encode(op: TreeOperation): String = op match {
    case InsertOperation(TreeIndex(i), Elem(c)) => "ins_%d_%d".format(i, c.toInt)
    case DeleteOperation(TreeIndex(i)) => "del_%d".format(i)
    case _ => error("no encoding for operation '%s' available".format(op.toString))
  }

  def decode(s: String): TreeOperation = {
    val InsExpr = new Regex("""ins_(\d+)_(\d+)""")
    val DelExpr = new Regex("""del_(\d+)""")
    s match {
      case InsExpr(i, c) => InsertOperation(TreeIndex(i.toInt), Elem(c.toInt.toChar))
      case DelExpr(i) => DeleteOperation(TreeIndex(i.toInt))
      case _ => error("no decoding for string '%s' available".format(s))
    }
  }
}
