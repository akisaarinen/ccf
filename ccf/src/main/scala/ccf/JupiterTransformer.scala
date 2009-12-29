package ccf

import ccf.operation.Operation

trait JupiterTransformer[T <: Operation] {
  def transformRemoteOpForLocalExecution(localOp: T, remoteOp: T, localIsPrimary: Boolean): T
  def createNoOp: T
}
