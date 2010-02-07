package textapp.server

import ccf.tree.operation.TreeOperation
import ccf.server.{ServerOperationInterceptor, Server}
import ccf.transport.{ClientId, ChannelId}
import textapp.TextDocument

class TextAppOperationInterceptor(document: TextDocument) extends ServerOperationInterceptor[TreeOperation] {
  override def currentStateFor(channelId: ChannelId): Any = {
    document
  }
  override def applyOperation(server: Server[TreeOperation], clientId: ClientId, channelId: ChannelId, op: TreeOperation): Unit = {
    document.applyOp(op)
  }
  override def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
  override def operationsForAllClients(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
}
