/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ccf.session

import ccf.OperationContext
import ccf.transport.{TransportRequestType, TransportResponse, TransportRequest}

sealed abstract class SessionRequest {
  val transportRequest: TransportRequest
  def successResponse(result: Option[Any]): SessionResponse
  def failureResponse(reason: String): SessionResponse
  protected def transportResponse(content: Option[Any]): TransportResponse = {
    TransportResponse(transportRequest.headers, content)
  }
}

trait DefaultSessionResponse extends SessionRequest {
  def successResponse(result: Option[Any]): SessionResponse = {
    sessionResponse(this, transportResponse(SessionResponse.successContent(None)), Left(Success(this, result)))
  }

  def failureResponse(reason: String): SessionResponse = {
    sessionResponse(this, transportResponse(SessionResponse.failureContent(reason)), Right(Failure(this, reason)))
  }

  private def sessionResponse(sessionRequest: SessionRequest, transportResponse: TransportResponse, result: Either[Success, Failure]): SessionResponse = {
    sessionRequest match {
      case JoinRequest(_) => JoinResponse(transportResponse, result)
      case PartRequest(_) => PartResponse(transportResponse, result)
      case InChannelRequest(_) => InChannelResponse(transportResponse, result)
      case OperationContextRequest(_) => OperationContextResponse(transportResponse, result)
    }
  }
}

object SessionRequest {
  def transportRequest(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): TransportRequest = TransportRequest(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "channelId" -> channelId.toString, "type" -> requestType),
    content
  )

  def sessionRequest(transportRequest: TransportRequest): SessionRequest = {
    transportRequest.header("type") match {
      case Some(TransportRequestType.join) => JoinRequest(transportRequest)
      case Some(TransportRequestType.part) => PartRequest(transportRequest)
      case Some(TransportRequestType.context) => OperationContextRequest(transportRequest)
      case Some(unknownRequestType) => error("Unknown request type: " + unknownRequestType)
      case None => error("No request type given")
    }
  }
}

abstract class SessionControlRequest extends SessionRequest

object SessionControlRequest {
  def transportRequest(s: Session, requestType: String, channelId: ChannelId): TransportRequest = {
    SessionRequest.transportRequest(s, requestType, channelId, Some(Map("channelId" -> channelId.toString)))
  }
}

case class JoinRequest(transportRequest: TransportRequest) extends SessionControlRequest with DefaultSessionResponse {
  require(transportRequest.header("type") == Some(TransportRequestType.join))
}

object JoinRequest {
  def apply(s: Session, channelId: ChannelId) = new JoinRequest(SessionControlRequest.transportRequest(s, TransportRequestType.join, channelId))
}

case class PartRequest(transportRequest: TransportRequest) extends SessionControlRequest with DefaultSessionResponse {
  require(transportRequest.header("type") == Some(TransportRequestType.part))
}

object PartRequest {
  def apply(s: Session, channelId: ChannelId) = new PartRequest(SessionControlRequest.transportRequest(s, TransportRequestType.part, channelId))
}

case class InChannelRequest(transportRequest: TransportRequest) extends SessionRequest with DefaultSessionResponse {
  require(transportRequest.header("type").isDefined)
  require(!TransportRequestType.sessionControlTypes.contains(transportRequest.header("type").get))
}

object InChannelRequest {
  def apply(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]) = new InChannelRequest(SessionRequest.transportRequest(s, requestType, channelId, content))
}

case class OperationContextRequest(transportRequest: TransportRequest) extends SessionRequest with DefaultSessionResponse {
  require(transportRequest.header("type") == Some(TransportRequestType.context))
}

object OperationContextRequest {
  def apply(s: Session, channelId: ChannelId, context: OperationContext) = new OperationContextRequest(SessionRequest.transportRequest(s, TransportRequestType.context, channelId, Some(context.encode)))
}
