package ccf.server

import ccf.operation.Operation
import ccf.transport.{TransportActor, ClientId, ChannelId}
import ccf.messaging.ConcurrentOperationMessage
import collection.mutable.ArrayBuffer

class ClientState[T <: Operation](val channel: ChannelId, val transport: TransportActor, synchronizer: OperationSynchronizer[T]) {
  def receive(msg: ConcurrentOperationMessage[T]): T = {
    synchronizer.receiveRemoteOperation(msg)
  }
  def send(op: T): ConcurrentOperationMessage[T] = {
    synchronizer.createLocalOperation(op)
  }
}
