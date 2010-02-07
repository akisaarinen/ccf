package textapp.client

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import java.awt.Dimension
import javax.swing.JTextArea
import javax.swing.text.SimpleAttributeSet
import textapp.Elem

class TextArea(initialText: String, 
               onInsert: List[(Int, Char)] => Unit, 
               onDelete: List[Int] => Unit) extends JTextArea {
  setPreferredSize(new Dimension(600, 400))
  setText(initialText)
  val listener = new TextComponentDocumentListener(onInsert, onDelete)
  getDocument.addDocumentListener(listener)

  def applyOp(op: TreeOperation) {
    getDocument.removeDocumentListener(listener)
    op match {
      case InsertOperation(TreeIndex(i), Elem(c)) =>         
        getDocument.insertString(i, c.toString, SimpleAttributeSet.EMPTY)
      case DeleteOperation(TreeIndex(i)) => 
        getDocument.remove(i, 1)
      case _ => error("unable to apply operation to text area ('%s')".format(op))
    }
    getDocument.addDocumentListener(listener)
  }
}
