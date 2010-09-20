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

import ccf.transport.TransportRequest
import ccf.OperationContext

abstract class SessionRequest(val transportRequest: TransportRequest)

object SessionRequest {
  val joinRequestType = "channel/join"
  val partRequestType = "channel/part"
  val contextRequestType = "channel/context"

  def transportRequest(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): TransportRequest = TransportRequest(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "channelId" -> channelId.id.toString, "type" -> requestType),
    content
  )

  def sessionRequest(transportRequest: TransportRequest): SessionRequest = {
    transportRequest.header("type") match {
      case Some(SessionRequest.joinRequestType) => new JoinRequest(transportRequest)
      case Some(SessionRequest.partRequestType) => new PartRequest(transportRequest)
      case Some(SessionRequest.contextRequestType) => new OperationContextRequest(transportRequest)
      case Some(unknownRequestType) => error("Unknown request type: " + unknownRequestType)
      case None => error("No request type given")
    }
  }
}

abstract class SessionControlRequest(transportRequest: TransportRequest) extends SessionRequest(transportRequest)

object SessionControlRequest {
  def transportRequest(s: Session, requestType: String, channelId: ChannelId): TransportRequest = {
    SessionRequest.transportRequest(s, requestType, channelId, Some(Map("channelId" -> channelId.toString)))
  }
}

class JoinRequest(transportRequest: TransportRequest) extends SessionControlRequest(transportRequest) {
  def this(s: Session, channelId: ChannelId) = {
    this(SessionControlRequest.transportRequest(s, SessionRequest.joinRequestType, channelId))
  }
}

class PartRequest(transportRequest: TransportRequest) extends SessionControlRequest(transportRequest) {
  def this(s: Session, channelId: ChannelId) = {
    this(SessionControlRequest.transportRequest(s, SessionRequest.partRequestType, channelId))
  }
}

class InChannelRequest(transportRequest: TransportRequest) extends SessionRequest(transportRequest) {
  def this(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]) = {
    this(SessionRequest.transportRequest(s, requestType, channelId, content))
  }
}

class OperationContextRequest(transportRequest: TransportRequest) extends InChannelRequest(transportRequest) {
  def this(s: Session, channelId: ChannelId, context: OperationContext) = {
     this(SessionRequest.transportRequest(s, SessionRequest.contextRequestType, channelId, Some(context.encode)))
  }
}


