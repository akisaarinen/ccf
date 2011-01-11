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

import ccf.transport.{TransportResponse, TransportRequest}
import ccf.messaging.OperationContext

sealed abstract class SessionRequest {
  val transportRequest: TransportRequest
  def successResponse(result: Option[Any]): SessionResponse
  def failureResponse(reason: String): SessionResponse
  def clientId: ClientId = ClientId(transportRequest.header("clientId").get).get
  protected def transportResponse(content: Option[Any]): TransportResponse = {
    TransportResponse(transportRequest.headers, content)
  }
}

trait DefaultSessionResponse extends SessionRequest {
  def successResponse(result: Option[Any]): SessionResponse = {
    sessionResponse(this, transportResponse(SessionResponseFactory.successContent(result)), Right(Success(this, result)))
  }

  def failureResponse(reason: String): SessionResponse = {
    sessionResponse(this, transportResponse(SessionResponseFactory.failureContent(reason)), Left(Failure(this, reason)))
  }

  private def sessionResponse(sessionRequest: SessionRequest, transportResponse: TransportResponse, result: Either[Failure, Success]): SessionResponse = {
    sessionRequest match {
      case JoinRequest(_, _) => JoinResponse(transportResponse, result)
      case PartRequest(_, _) => PartResponse(transportResponse, result)
      case req: InChannelRequest => InChannelResponse(transportResponse, result)
      case OperationContextRequest(_, _) => OperationContextResponse(transportResponse, result)
    }
  }
}

object SessionRequest {
  def apply(transportRequest: TransportRequest): SessionRequest = SessionRequestFactory.sessionRequest(transportRequest)
}

abstract class SessionControlRequest extends SessionRequest

object SessionControlRequest {
  def transportRequest(s: Session, requestType: String, channelId: ChannelId): TransportRequest = {
    SessionRequestFactory.transportRequest(s, requestType, channelId, Some(Map(SessionRequestFactory.ChannelIdKey -> channelId.toString)))
  }
}

case class JoinRequest(transportRequest: TransportRequest, channelId: ChannelId) extends SessionControlRequest with DefaultSessionResponse {
  require(transportRequest.header(SessionRequestFactory.TypeKey) == Some(SessionRequestFactory.JoinType))
}

object JoinRequest {
  def apply(s: Session, channelId: ChannelId) = new JoinRequest(SessionControlRequest.transportRequest(s, SessionRequestFactory.JoinType, channelId), channelId)
}

case class PartRequest(transportRequest: TransportRequest, channelId: ChannelId) extends SessionControlRequest with DefaultSessionResponse {
  require(transportRequest.header(SessionRequestFactory.TypeKey) == Some(SessionRequestFactory.PartType))
}

object PartRequest {
  def apply(s: Session, channelId: ChannelId) = new PartRequest(SessionControlRequest.transportRequest(s, SessionRequestFactory.PartType, channelId), channelId)
}

case class InChannelRequest(transportRequest: TransportRequest, requestType: String, channelId: ChannelId, content: Option[Any])
        extends SessionRequest with DefaultSessionResponse {
  require(transportRequest.header(SessionRequestFactory.TypeKey).isDefined)
  require(!SessionRequestFactory.SessionControlTypes.contains(transportRequest.header(SessionRequestFactory.TypeKey).get))
}

object InChannelRequest {
  def apply(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]) =
    new InChannelRequest(SessionRequestFactory.transportRequest(s, requestType, channelId, content), requestType, channelId, content)
}

case class OperationContextRequest(transportRequest: TransportRequest, channelId: ChannelId) extends SessionRequest with DefaultSessionResponse {
  require(transportRequest.header(SessionRequestFactory.TypeKey) == Some(SessionRequestFactory.OperationContextType))
}

object OperationContextRequest {
  def apply(s: Session, channelId: ChannelId, context: OperationContext) =
    new OperationContextRequest(SessionRequestFactory.transportRequest(s, SessionRequestFactory.OperationContextType, channelId, Some(context.encode)), channelId)
}
