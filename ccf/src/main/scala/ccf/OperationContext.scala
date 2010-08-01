package ccf

import ccf.operation.Operation

case class OperationContext[T <: Operation](val op: T, val localMsgSeqNo: Int, val remoteMsgSeqNo: Int) {
  def encode: Any = Map("op" -> op.encode, "localMsgSeqNo" -> localMsgSeqNo, "remoteMsgSeqNo" -> remoteMsgSeqNo)
}
