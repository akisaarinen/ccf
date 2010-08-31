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

import ccf.transport.Request
import ccf.OperationContext

abstract class AbstractRequest {
  protected def request(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): Request = Request(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "channelId" -> channelId.toString, "type" -> requestType),
    content
  )
}

object AbstractRequest {
  val joinRequestType = "channel/join"
  val partRequestType = "channel/part"
  val contextRequestType = "channel/context"
}

abstract class SessionControlRequest extends AbstractRequest {
  protected def request(s: Session, channelId: ChannelId): Request = request(s, requestType, channelId, content(channelId))
  private def content(channelId: ChannelId) = Some(Map("channelId" -> channelId.toString))
  protected val requestType: String
}

class JoinRequest extends SessionControlRequest {
  def create(s: Session, channelId: ChannelId): Request = request(s, channelId)
  protected val requestType = AbstractRequest.joinRequestType
}

class PartRequest extends SessionControlRequest  {
  def create
  (s: Session, channelId: ChannelId): Request = request(s, channelId)
  protected val requestType = AbstractRequest.partRequestType
}

class InChannelRequest extends AbstractRequest {
  def create(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): Request =
    request(s, requestType, channelId, content)
}

class OperationContextRequest extends InChannelRequest {
  def create(s: Session, channelId: ChannelId, context: OperationContext): Request =
    request(s, requestType, channelId, Some(context.encode))
  protected val requestType = AbstractRequest.contextRequestType
}


