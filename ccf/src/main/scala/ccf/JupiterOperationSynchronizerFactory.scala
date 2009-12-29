package ccf

import ccf.operation.Operation

class JupiterOperationSynchronizerFactory[T <: Operation](isPrimary: Boolean, transformer: JupiterTransformer[T]) extends OperationSynchronizerFactory[T] {
  def createSynchronizer = new JupiterOperationSynchronizer(isPrimary, transformer)
}
