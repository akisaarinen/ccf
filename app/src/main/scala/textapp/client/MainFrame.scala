package textapp.client

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import javax.swing.JFrame
import textapp.{TextDocument, Elem}

class MainFrame(document: TextDocument, sendToServer: TreeOperation => Unit) extends JFrame("libccf test application") {
  val textArea = new TextArea(document.text, onInsert, onDelete)
  getContentPane().add(textArea)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  pack()
  setVisible(true)

  def applyOp(op: TreeOperation) {
    textArea.applyOp(op)
  }

  private def onInsert(items: List[(Int, Char)]) {
    items.foreach { case (i, c) => 
      sendToServer(InsertOperation(TreeIndex(i), Elem(c)))
    }
  }

  private def onDelete(items: List[Int]) {
    items.foreach { i =>
      sendToServer(DeleteOperation(TreeIndex(i)))
    }
  }
}
