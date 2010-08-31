/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

class ClientApp(hostname: String, port: Int) {
  private val clientSync = new JupiterOperationSynchronizer(false, JupiterTreeTransformation)
  private val messageCoder = new MessageCoder
  
  val httpClient = new HttpClient(hostname, port, ClientId.randomId)
  val initialText = httpClient.join
  val document = new TextDocument(initialText)
  val frame = new MainFrame(hostname, port, document, sendToServer)
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
            case msg: ConcurrentOperationMessage =>
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
