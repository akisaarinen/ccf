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
import java.util.{UUID, Timer, TimerTask}
import ccf.tree.operation.TreeOperation
import java.net.URL
import ccf.transport.BASE64EncodingSerializer
import textapp.{TextAppOperationDecoder, TextDocument}

class ClientApp(hostname: String, port: Int) {
  private val connection = HttpConnection.create(new URL("http://" + hostname + ":" + port + "/textapp/"))
  private val clientId = ClientId.randomId
  private val channelId = new ChannelId(new UUID(1,1))
  private val version = Version(1,0)
  private val sa = new SessionActor(connection, clientId, version)

  private val serializer = BASE64EncodingSerializer
  private val decoder = new TextAppOperationDecoder

  val document = join
  private val clientSync = new JupiterOperationSynchronizer(false, JupiterTreeTransformation)

  val frame = new MainFrame(hostname, port, document, sendToServer)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.pack()
  frame.setVisible(true)

  private def join: TextDocument = {
    sa !? Message.Join(channelId) match {
      case Right(Success(_, Some(data))) => serializer.deserialize[TextDocument](data.toString)
      case m => error("Error in join: " + m)
    }
  }

  val timer = new Timer
  timer.scheduleAtFixedRate(syncTask, 0, 500)

  private def syncTask = new TimerTask {
    def run = Utils.invokeAndWait { () =>
       (sa !? Message.InChannel("channel/getMsgs", channelId, Some(0))) match {
         case Right(Success(_, Some(encodedMessages: List[_]))) => {
           val messageMaps:List[Map[String, String]] = encodedMessages.asInstanceOf[List[Map[String, String]]]
           val messages = messageMaps.map(ccf.messaging.Message(_, decoder))
           messages.foreach { msg =>
             val op = clientSync.receiveRemoteOperation(msg.asInstanceOf[OperationContext])
             applyOperationLocally(op)
           }
         }
         case Right(Success(_, None)) =>
         case Left(Failure(_, reason)) => println(reason); throw new RuntimeException(reason)
       }
    }
  }

  private def applyOperationLocally(op: TreeOperation) {
    document.applyOp(op)
    frame.applyOp(op)
  }
  
  private def sendToServer(op: TreeOperation) {
    document.applyOp(op)
    val context = clientSync.createLocalOperation(op)
    // TODO: handle errors
    sa !? Message.OperationContext(channelId, context)
  }
}
