package ccf.server

import ccf.operation.Operation
import ccf.transport.{ClientId, ChannelId}
import ccf.messaging.ConcurrentOperationMessage
import collection.mutable.ArrayBuffer

class ClientState[T <: Operation](val channel: ChannelId, synchronizer: OperationSynchronizer[T]) {
  def receive(msg: ConcurrentOperationMessage[T]): T = {
    synchronizer.receiveRemoteOperation(msg)
  }
  def send(op: T): ConcurrentOperationMessage[T] = {
    synchronizer.createLocalOperation(op)
  }
}
