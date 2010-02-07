package textapp.client

import ccf.JupiterOperationSynchronizer
import ccf.messaging.ConcurrentOperationMessage
import ccf.tree.JupiterTreeTransformation
import ccf.tree.indexing.TreeIndex
import ccf.transport.ClientId
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import java.util.{Timer, TimerTask}
import javax.swing.JFrame
import MessageCoding.encode

class ClientApp {
  private val clientSync = new JupiterOperationSynchronizer[TreeOperation](false, JupiterTreeTransformation)
  
  val frame = new JFrame("TextApp")
  val httpClient = new HttpClient(ClientId.randomId)

  val initialText = httpClient.join
  val document = new TextDocument(initialText)
  val textArea = new TextArea(initialText, onInsert, onDelete)

  frame.getContentPane().add(textArea)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.pack()
  frame.setVisible(true)

  val timer = new Timer
  timer.scheduleAtFixedRate(syncTask, 0, 500)

  private def syncTask = new TimerTask {
    def run = Utils.invokeAndWait { () => 
      httpClient.get match {
        case (hash, msgs) => {
          msgs.foreach { msg => 
            val op = clientSync.receiveRemoteOperation(msg)
            applyOperationLocally(op)
          }
          if (document.hash != hash) error("Hash differs after sync :(")
        }
      }
    }
  }

  private def applyOperationLocally(op: TreeOperation) {
      textArea.applyOp(op)
      document.applyOp(op)
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
  
  private def sendToServer(op: TreeOperation) {
    document.applyOp(op)
    val msg = clientSync.createLocalOperation(op)
    httpClient.add(encode(msg))
  }
}