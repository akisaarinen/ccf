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

import ccf.transport.{Request, Response}
import ccf.OperationContext

case object Shutdown

trait Message {
  def send(s: Session): (Session, Option[Response])
  protected def send(s: Session, request: Request, channels: Set[ChannelId]): (Session, Option[Response]) = {
    val nextSession = s.next(channels)
    val response = s.connection.send(request)
    (nextSession, response)
  }
}

case class Join(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = 
    if (!s.channels(channelId)) send(s, new JoinRequest().create(s, channelId), s.channels + channelId) else (s, None)
}
case class Part(channelId: ChannelId) extends Message {
  def send(s: Session): (Session, Option[Response]) = 
    if (s.channels(channelId)) send(s, new PartRequest().create(s, channelId), s.channels - channelId) else (s, None)
}
case class InChannelMessage(requestType: String, channelId: ChannelId, content: Option[Any]) extends Message {
  def send(s: Session): (Session, Option[Response]) =
    if (s.channels(channelId)) send(s, new InChannelRequest().create(s, requestType, channelId, content), s.channels) else (s, None)
}
case class OperationContextMessage(channelId: ChannelId, context: OperationContext) extends Message {
  def send(s: Session): (Session, Option[Response]) = {
    if (s.channels(channelId)) send(s, new OperationContextRequest().create(s, channelId, context), s.channels) else (s, None)
  }
}
