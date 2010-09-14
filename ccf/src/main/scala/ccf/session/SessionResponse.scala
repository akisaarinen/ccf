package ccf.session

import ccf.transport.TransportResponse

sealed abstract class SessionResponse {
  val transportResponse: TransportResponse
  val result: Either[Success, Failure] 
}

case class JoinResponse(transportResponse: TransportResponse, result: Either[Success, Failure]) extends SessionResponse

case class PartResponse(transportResponse: TransportResponse, result: Either[Success, Failure]) extends SessionResponse

case class InChannelResponse(transportResponse: TransportResponse, result: Either[Success, Failure]) extends SessionResponse

case class OperationContextResponse(transportResponse: TransportResponse, result: Either[Success, Failure]) extends SessionResponse
