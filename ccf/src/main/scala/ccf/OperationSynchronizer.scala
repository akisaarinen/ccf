package ccf

import ccf.operation.Operation
import ccf.messaging.ConcurrentOperationMessage

trait OperationSynchronizer[T <: Operation] {
  def resetToInitialState: Unit
  def createLocalOperation(operation: T): ConcurrentOperationMessage[T]
  def receiveRemoteOperation(message: ConcurrentOperationMessage[T]): T
}
