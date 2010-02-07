package textapp.client

import ccf.JupiterOperationSynchronizer
import ccf.tree.JupiterTreeTransformation
import ccf.transport.ClientId
import ccf.tree.operation.TreeOperation
import java.util.{Timer, TimerTask}
import textapp.messaging.MessageCoder
import textapp.TextDocument
import ccf.messaging.ConcurrentOperationMessage
import javax.swing.JFrame

class ClientApp {
  private val clientSync = new JupiterOperationSynchronizer[TreeOperation](false, JupiterTreeTransformation)
  private val messageCoder = new MessageCoder
  
  val httpClient = new HttpClient(ClientId.randomId)
  val initialText = httpClient.join
  val document = new TextDocument(initialText)
  val frame = new MainFrame(document, sendToServer)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.pack()
  frame.setVisible(true)
  
  val timer = new Timer
  timer.scheduleAtFixedRate(syncTask, 0, 500)

  private def syncTask = new TimerTask {
    def run = Utils.invokeAndWait { () => 
      httpClient.get match {
        case (hash, msgs) => {
          msgs.foreach { _ match {
            case msg: ConcurrentOperationMessage[TreeOperation] =>
              val op = clientSync.receiveRemoteOperation(msg)
              applyOperationLocally(op)
            case msg =>
              error("Received unknown message type (%s)".format(msg.toString))
          }}
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
    httpClient.add(messageCoder.encode(msg))
  }
}
