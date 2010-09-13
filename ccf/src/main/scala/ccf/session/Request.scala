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

import ccf.transport
import ccf.OperationContext

abstract class AbstractRequest(val transportRequest: transport.Request)

object AbstractRequest {
  val joinRequestType = "channel/join"
  val partRequestType = "channel/part"
  val contextRequestType = "channel/context"

  def transportRequest(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): transport.Request = transport.Request(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "channelId" -> channelId.toString, "type" -> requestType),
    content
  )
}

abstract class SessionControlRequest(transportRequest: transport.Request) extends AbstractRequest(transportRequest)

object SessionControlRequest {
  def transportRequest(s: Session, requestType: String, channelId: ChannelId): transport.Request = {
    AbstractRequest.transportRequest(s, requestType, channelId, Some(Map("channelId" -> channelId.toString)))
  }
}

class JoinRequest(transportRequest: transport.Request) extends SessionControlRequest(transportRequest) {
  def this(s: Session, channelId: ChannelId) = {
    this(SessionControlRequest.transportRequest(s, AbstractRequest.joinRequestType, channelId))
  }
}

class PartRequest(transportRequest: transport.Request) extends SessionControlRequest(transportRequest) {
  def this(s: Session, channelId: ChannelId) = {
    this(SessionControlRequest.transportRequest(s, AbstractRequest.partRequestType, channelId))
  }
}

class InChannelRequest(transportRequest: transport.Request) extends AbstractRequest(transportRequest) {
  def this(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]) = {
    this(AbstractRequest.transportRequest(s, requestType, channelId, content))
  }
}

class OperationContextRequest(transportRequest: transport.Request) extends InChannelRequest(transportRequest) {
  def this(s: Session, channelId: ChannelId, context: OperationContext) = {
     this(AbstractRequest.transportRequest(s, AbstractRequest.contextRequestType, channelId, Some(context.encode)))
  }
}


