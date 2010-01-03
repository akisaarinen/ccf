package textapp.client

import ccf.tree.indexing.TreeIndex
import ccf.transport.ClientId
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import javax.swing.JFrame
import OperationCoding.{encode, decode}

class ClientApp {
  val frame = new JFrame("TextApp")
  val httpClient = new HttpClient(ClientId.randomId)

  val initialText = httpClient.join
  val document = new TextDocument(initialText)
  val textArea = new TextArea(initialText, onInsert, onDelete)

  frame.getContentPane().add(textArea)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.pack()
  frame.setVisible(true)

  import java.util.Timer
  val timer = new Timer
  timer.scheduleAtFixedRate(syncTask, 0, 500)

  def onInsert(items: List[(Int, Char)]) {
    items.foreach { case (i, c) => 
      //println("+ %c at %d".format(c, i))
      sendToServer(InsertOperation(TreeIndex(i), Elem(c)))
    }
  }

  def onDelete(items: List[Int]) {
    items.foreach { i =>
      //println("- %d".format(i))
      sendToServer(DeleteOperation(TreeIndex(i)))
    }
  }
  
  private def sendToServer(op: TreeOperation) {
    document.applyOp(op)
    httpClient.add(encode(op))
  }

  import java.util.TimerTask
  def syncTask = new TimerTask {
    def run = Utils.invokeAndWait { () => 
      httpClient.get match {
        case (hash, ops) => {
          ops.foreach { op => 
            textArea.applyOp(op)
            document.applyOp(op)
          }
          if (document.hash != hash) error("Hash differs after sync :(")
        }
      }
    }
  }
}
