package ccf.session

import ccf.operation.Operation

trait OperationDecoder[T <: Operation] {
  def decode(any: Any): T
}