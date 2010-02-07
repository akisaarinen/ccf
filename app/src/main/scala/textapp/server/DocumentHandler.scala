package textapp.server

import ccf.messaging.ConcurrentOperationMessage
import ccf.server.Server
import ccf.transport.Event
import ccf.transport.{ClientId, ChannelId}
import ccf.tree.JupiterTreeTransformation
import ccf.tree.operation.TreeOperation
import ccf.JupiterOperationSynchronizerFactory
import textapp.TextDocument

class DocumentHandler {
  private val document = new TextDocument("")
  private var messages = List[Event.Msg[TreeOperation]]()
    
  private val factory = new JupiterOperationSynchronizerFactory(true, JupiterTreeTransformation)
  private val interceptor = new TextAppOperationInterceptor(document)
  private val transport = new TextAppTransportActor(onMessageToClient)
  private val server = new Server[TreeOperation](factory, interceptor, transport)

  private def onMessageToClient(msg: Event.Msg[TreeOperation]): Unit = messages.synchronized {
    messages = messages ::: List(msg)
  }

  def onJoin(clientId: ClientId, channelId: ChannelId): TextDocument = {
    server !? Event.Join(clientId, channelId) match {
      case Event.State(_, _, state) => state.asInstanceOf[TextDocument]
      case _ => error("Join failed")
    }
  }

  def onQuit(clientId: ClientId, channelId: ChannelId) {
    server !? Event.Quit(clientId, channelId)
  }
  
  def onMsg(clientId: ClientId, channelId: ChannelId, msg: ConcurrentOperationMessage[TreeOperation]) {
    server !? Event.Msg(clientId, channelId, msg)
  }

  def getMessages(clientId: ClientId): (List[ConcurrentOperationMessage[TreeOperation]], TextDocument) = messages.synchronized {
    def isForClient(msg: Event.Msg[_]) = msg match {
      case Event.Msg(id, _, _) if (id == clientId) => true
      case _ => false
    }

    val msgsForClient = messages.filter(isForClient(_))
    val msgsNotForClient = messages.filter(!isForClient(_))
    messages = msgsNotForClient
    val msgsAsCom = msgsForClient.map(_.msg.asInstanceOf[ConcurrentOperationMessage[TreeOperation]])
    (msgsAsCom, document)
  }
}
