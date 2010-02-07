package textapp.client

import ccf.JupiterOperationSynchronizer
import ccf.tree.JupiterTreeTransformation
import ccf.transport.ClientId
import ccf.tree.operation.TreeOperation
import java.util.{Timer, TimerTask}
import textapp.messaging.MessageCoding.encode
import textapp.TextDocument

class ClientApp {
  private val clientSync = new JupiterOperationSynchronizer[TreeOperation](false, JupiterTreeTransformation)
  
  val httpClient = new HttpClient(ClientId.randomId)
  val initialText = httpClient.join
  val document = new TextDocument(initialText)
  val frame = new MainFrame(document, sendToServer)
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
    document.applyOp(op)
    frame.applyOp(op)
  }
  
  private def sendToServer(op: TreeOperation) {
    document.applyOp(op)
    val msg = clientSync.createLocalOperation(op)
    httpClient.add(encode(msg))
  }
}
