package ccf.session

import ccf.operation.Operation

trait OperationEncoder[T <: Operation] {
  def encode(op: T): Any
}