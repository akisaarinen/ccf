package ccf.messaging

import ccf.operation.Operation

class Message[T <: Operation]
case class ConcurrentOperationMessage[T <: Operation](val op: T, val localMessage: Int, val expectedRemoteMessage: Int) extends Message[T]
class ErrorMessage[T <: Operation](val reason: String) extends Message[T]
case class ChannelShutdown[T <: Operation](override val reason: String) extends ErrorMessage[T](reason)
