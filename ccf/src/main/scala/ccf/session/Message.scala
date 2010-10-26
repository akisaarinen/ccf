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

trait Message {
  def send(s: Session): (Session, Option[SessionResponse])
  protected def send(s: Session, request: SessionRequest, channels: Set[ChannelId]): (Session, Option[SessionResponse]) = {
    val nextSession = s.next(channels)
    val response = s.connection.send(request.transportRequest).map(SessionResponse(_, request))
    (nextSession, response)
  }
}

object Message {
  def apply(request: SessionRequest): Message = MessageFactory.message(request)

  case class Join(channelId: ChannelId) extends Message {
    def send(s: Session): (Session, Option[SessionResponse]) =
      if (!s.channels(channelId)) send(s, JoinRequest(s, channelId), s.channels + channelId) else (s, None)
  }

  case class Part(channelId: ChannelId) extends Message {
    def send(s: Session): (Session, Option[SessionResponse]) =
      if (s.channels(channelId)) send(s, PartRequest(s, channelId), s.channels - channelId) else (s, None)
  }

  case class InChannel(requestType: String, channelId: ChannelId, content: Option[Any]) extends Message {
    def send(s: Session): (Session, Option[SessionResponse]) =
      if (s.channels(channelId)) send(s, InChannelRequest(s, requestType, channelId, content), s.channels) else (s, None)
  }

  case class OperationContext(channelId: ChannelId, context: ccf.messaging.OperationContext) extends Message {
    def send(s: Session): (Session, Option[SessionResponse]) = {
      if (s.channels(channelId)) send(s, OperationContextRequest(s, channelId, context), s.channels) else (s, None)
    }
  }

  case object Shutdown
}

