package textapp.client

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import textapp.Elem
import java.awt.{Color, Dimension}
import javax.swing.JTextPane
import javax.swing.text.{StyleConstants, SimpleAttributeSet, StyleContext}

class TextArea(initialText: String,
               onInsert: List[(Int, Char)] => Unit, 
               onDelete: List[Int] => Unit) extends JTextPane {
  private val defaultStyleContext = StyleContext.getDefaultStyleContext
  private val remoteEditColor = new Color(255, 0, 0)
  private val remoteEditAttributes = defaultStyleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, remoteEditColor);
  private val localEditColor = new Color(0, 150, 0)
  private val localEditAttributes = defaultStyleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, localEditColor);

  setPreferredSize(new Dimension(600, 400))
  setText(initialText)
  val listener = new TextComponentDocumentListener(onInsert, onDelete)
  getDocument.addDocumentListener(listener)

  override def getInputAttributes = {
    val originalInputAttributes = super.getInputAttributes
    StyleConstants.setForeground(originalInputAttributes, localEditColor)
    originalInputAttributes
  }

  def applyOp(op: TreeOperation) {
    getDocument.removeDocumentListener(listener)
    op match {
      case InsertOperation(TreeIndex(i), Elem(c)) =>         
        getDocument.insertString(i, c.toString, remoteEditAttributes)
      case DeleteOperation(TreeIndex(i)) =>
        getDocument.remove(i, 1)
      case _ => error("unable to apply operation to text area ('%s')".format(op))
    }
    getDocument.addDocumentListener(listener)
  }
}
