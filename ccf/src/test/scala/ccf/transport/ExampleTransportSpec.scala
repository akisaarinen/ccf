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

import ccf.tree.operation.TreeOperation
import ccf.messaging.Message
import ccf.server.Server
import org.specs.Specification
import org.specs.mock.Mockito

object ExampleTransportSpec extends Specification with Mockito {
  val listener = mock[ExampleTransportListener]
  val transport = mock[TransportActor]
  val t = new ExampleTransport(listener)

  "ExampleTransport" should {
    val clientId = ClientId.randomId
    val channelId = ChannelId.randomId

    "initialize" in {
      val server = mock[Server]
      t.initialize(server)
      there was one(listener).onInitialize(server)
    }

    "join" in {
      listener.onJoin(clientId, channelId) returns 123
      t !? Event.Join(clientId, channelId) must equalTo(Event.State(clientId, channelId, 123))
    }

    "quit" in {
      t !? Event.Quit(clientId, channelId) must equalTo(Event.Ok())
      there was one(listener).onQuit(clientId, channelId)
    }

    "request sync" in {
      t !? Event.Sync(clientId, channelId) must equalTo(Event.Ok())
      there was one(listener).onSync(clientId, channelId)
    }

    "pass a message" in {
      val msg = mock[Message]
      t !? Event.Msg(clientId, channelId, msg) must equalTo(Event.Ok())
      there was one(listener).onMsg(clientId, channelId, msg)
    }

    "return Event.Error() on unknown message" in {
      case class UnknownMsg()
      t !? UnknownMsg() must haveClass[Event.Error]
    }
  }
}
