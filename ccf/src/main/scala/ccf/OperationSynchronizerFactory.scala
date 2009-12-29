package ccf

import ccf.operation.Operation

abstract class OperationSynchronizerFactory[T <: Operation] {
  def createSynchronizer: OperationSynchronizer[T]
}

