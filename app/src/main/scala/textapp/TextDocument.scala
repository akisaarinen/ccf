package textapp

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import java.security.MessageDigest

class TextDocument(initialText: String) {
  private val buffer = new StringBuffer(initialText)
  private val md = MessageDigest.getInstance("MD5")
  def applyOp(op: TreeOperation): Unit = op match {
    case InsertOperation(TreeIndex(i), Elem(c)) => buffer.insert(i, c)
    case DeleteOperation(TreeIndex(i)) => buffer.deleteCharAt(i)
    case _ => error("Unknown operation " + op)
  }
  def text = buffer.toString
  def hash: String = {
    md.reset
    md.update(text.getBytes)
    md.digest.map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }
}

