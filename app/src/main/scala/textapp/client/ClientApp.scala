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
import ccf.messaging.OperationContext
import javax.swing.JFrame
import ccf.transport.http.HttpConnection
import ccf.session._
import ccf.client.Client
import java.util.{UUID, Timer, TimerTask}
import ccf.tree.operation.TreeOperation
import java.net.URL
import ccf.transport.BASE64EncodingSerializer
import textapp.document.{TextAppOperationDecoder, TextDocument}

class ClientApp(hostname: String, port: Int) {
  private val url = new URL("http://" + hostname + ":" + port + "/textapp/")
  private val version = Version(1,0)
  private val client = new Client[TextDocument](url, version, new TextAppOperationDecoder)

  private val channel = new ChannelId(new UUID(1,1))

  val document = client.join(channel)
  val frame = new MainFrame(hostname, port, document, sendToServer)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.pack()
  frame.setVisible(true)
  val timer = new Timer
  timer.scheduleAtFixedRate(syncTask, 0, 500)

  private def syncTask = new TimerTask {
    def run = Utils.invokeAndWait { () =>
      client.getOperations(channel).foreach(applyOperationLocally(_))
    }
  }

  private def applyOperationLocally(op: TreeOperation) {
    document.applyOp(op)
    frame.applyOp(op)
  }
  
  private def sendToServer(op: TreeOperation) {
    document.applyOp(op)
    client.sendOperation(channel, op)
  }
}
