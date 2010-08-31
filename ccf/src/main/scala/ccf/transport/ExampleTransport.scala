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
import ccf.server.Server

trait ExampleTransportListener {
  def onInitialize(server: Server): Unit
  def onJoin(clientId: ClientId, channelId: ChannelId): Any
  def onQuit(clientId: ClientId, channelId: ChannelId): Unit
  def onSync(clientId: ClientId, channelId: ChannelId): Unit
  def onMsg(clientId: ClientId, channelId: ChannelId, msg: Message): Unit
}

class ExampleTransport(listener: ExampleTransportListener) extends TransportActor {
  start
  def act() = loop { react {
    case Event.Join(client, channel) => {
      val state = listener.onJoin(client, channel)
      reply(Event.State(client, channel, state))
    }
    case Event.Quit(client, channel) => {
      listener.onQuit(client, channel)
      reply(Event.Ok())
    }
    case Event.Sync(client, channel) => {
      listener.onSync(client, channel)
      reply(Event.Ok())
    }
    case Event.Msg(client, channel, msg) => {
      listener.onMsg(client, channel, msg)
      reply(Event.Ok())
    }
    case _ => reply(Event.Error("Unknown message"))
  }}

  def initialize(server: Server) = listener.onInitialize(server)
}
