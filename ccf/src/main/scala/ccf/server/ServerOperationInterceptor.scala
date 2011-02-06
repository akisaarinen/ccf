package ccf.server

import java.io.Serializable
import ccf.session.{ClientId, ChannelId}
import ccf.tree.operation.TreeOperation

trait ServerOperationInterceptor {
  def currentStateFor(channelId: ChannelId): Serializable
  def applyOperation(shutdownListener: ShutdownListener, clientId: ClientId, channelId: ChannelId, op: TreeOperation): Unit
  def applyOperation(shutdownListener: ShutdownListener, channelId: ChannelId, op: TreeOperation) = {}
  def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation]
  def operationsForAllClients(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation]
  def operationsForAllClients(channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
}