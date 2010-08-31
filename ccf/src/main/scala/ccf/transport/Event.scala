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

package ccf.transport

import ccf.messaging.Message

object Event {
  case class Ok()
  case class Error(reason: String)

  case class Join(clientId: ClientId, channelId: ChannelId)
  case class State[T](clientId: ClientId, channelId: ChannelId, state: T)
  case class Quit(clientId: ClientId, channelId: ChannelId)
  case class ShutdownChannel(channelId: ChannelId, reason: String)
  case class Sync(clientId: ClientId, channelId: ChannelId)
  case class Msg(clientId: ClientId, channelId: ChannelId, msg: Message)
}
