package ccf.server

import java.io.Serializable
import ccf.session.{ClientId, ChannelId}
import ccf.tree.operation.TreeOperation

trait ServerOperationInterceptor {
  def currentStateFor(channelId: ChannelId): Serializable
  def applyOperation(shutdownListener: ShutdownListener, channelId: ChannelId, op: TreeOperation): Unit
  def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
  def operationsForAllClients(channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
}