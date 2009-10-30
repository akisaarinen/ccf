package ccf.messaging

import ccf.operation.Operation

case class ConcurrentOperationMessage[T <: Operation](val op: T, val localMessage: Int, val expectedRemoteMessage: Int)
