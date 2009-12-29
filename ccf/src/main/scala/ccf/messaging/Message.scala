package ccf.messaging

import ccf.operation.Operation

case class Message[T <: Operation]()
case class ConcurrentOperationMessage[T <: Operation](val op: T, val localMessage: Int, val expectedRemoteMessage: Int) extends Message[T]()
case class ErrorMessage[T <: Operation](reason: String) extends Message[T]()
case class ChannelShutdown[T <: Operation](override val reason: String) extends ErrorMessage[T](reason)
