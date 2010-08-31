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

package ccf.server

import ccf.messaging.{ChannelShutdown, ConcurrentOperationMessage}
import ccf.transport.{ClientId, ChannelId, Event}
import ccf.transport.TransportActor
import ccf.OperationSynchronizer
import ccf.OperationSynchronizerFactory
import ccf.tree.operation.TreeOperation

import org.specs.Specification
import org.specs.mock.Mockito
import org.mockito.Matchers._

class ServerSpec extends Specification with Mockito {
  val synchronizer = mock[OperationSynchronizer]
  val transport = mock[TransportActor]
  val factory = mock[OperationSynchronizerFactory]
  factory.createSynchronizer returns synchronizer
  val interceptor = mock[ServerOperationInterceptor]
  interceptor.operationsForCreatingClient(anyObject[ClientId], anyObject[ChannelId], anyObject[TreeOperation]) returns List()
  interceptor.operationsForAllClients(anyObject[ClientId], anyObject[ChannelId], anyObject[TreeOperation]) returns List()
  val server = new Server(factory, interceptor, transport)

  "Server with no clients" should {
    "initialize transport" in {
      there was one(transport).initialize(server)
    }

    "accept a client and return current state" in {
      val client = ClientId.randomId
      val channel = ChannelId.randomId
      case class MyStateClass(i: Int)
      val myState = MyStateClass(123)
      interceptor.currentStateFor(channel) returns myState
      server !? Event.Join(client, channel) must equalTo(Event.State(client, channel, myState))
      server.clients.contains(client) must equalTo(true)
    }

    "reply with error to unknown message" in {
      case class MyMessage()
      server !? MyMessage() must haveClass[Event.Error]
    }
  }

  "Server with registered clients on one channel" should {
    val client1 = ClientId.randomId
    val client2 = ClientId.randomId
    val client3 = ClientId.randomId
    val channel = ChannelId.randomId

    doBefore {
      server !? Event.Join(client1, channel)
      server !? Event.Join(client2, channel)
      server !? Event.Join(client3, channel)
    }
    
    "quit a joined client" in {
      server !? Event.Quit(client1, channel) must equalTo(Event.Ok())
      server.clients.contains(client1) must equalTo(false)
    }

    "not quit an unknown client" in {
      server !? Event.Quit(ClientId.randomId, channel) must haveClass[Event.Error]
    }

    "not allow a client to join another channel before quitting" in {
      server !? Event.Join(client1, ChannelId.randomId) must haveClass[Event.Error]
    }

    "return state for rejoin to already joined channel" in {
      case class MyStateClass(i: Int)
      val myState = MyStateClass(123)
      interceptor.currentStateFor(channel) returns myState
      server !? Event.Join(client1, channel) must equalTo(Event.State(client1, channel, myState))
    }

    "return error if interceptor throws an exception" in {
      interceptor.currentStateFor(channel) throws new RuntimeException("")
      server !? Event.Join(client1, channel) must haveClass[Event.Error]
    }
  }

  "Server with registered clients on two channels" should {
    val channel = ChannelId.randomId
    val client1 = ClientId.randomId
    val client2 = ClientId.randomId
    val otherChannel = ChannelId.randomId
    val clientInOtherChannel = ClientId.randomId
    val msg = mock[ConcurrentOperationMessage]
    val op = mock[TreeOperation]
    synchronizer.receiveRemoteOperation(msg) returns op
    synchronizer.createLocalOperation(op) returns msg
      
    doBefore {
      server !? Event.Join(client1, channel)
      server !? Event.Join(client2, channel)
      server !? Event.Join(clientInOtherChannel, otherChannel)
    }

    "propagate messages from a client to others in same channel" in {
      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      there was one(transport) !! Event.Msg(client2, channel, msg)
      there was one(transport).initialize(server)
      there was no(transport)
    }

    "not accept message if client has not joined the channel" in {
      server !? Event.Msg(clientInOtherChannel, channel, msg) must haveClass[Event.Error]
    }

    "propagate operations for creating client" in {
      val creationOp = mock[TreeOperation]
      val creationMsg = mock[ConcurrentOperationMessage]
      synchronizer.receiveRemoteOperation(creationMsg) returns creationOp
      synchronizer.createLocalOperation(creationOp) returns creationMsg
      interceptor.operationsForCreatingClient(client1, channel, op) returns List(creationOp, creationOp)

      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      there was two(transport) !! Event.Msg(client1, channel, creationMsg)
      there was one(interceptor).applyOperation(server, client1, channel, op)
    }

    "propagate operations for all clients" in {
      val forAllOp = mock[TreeOperation]
      val forAllMsg = mock[ConcurrentOperationMessage]
      synchronizer.receiveRemoteOperation(forAllMsg) returns forAllOp
      synchronizer.createLocalOperation(forAllOp) returns forAllMsg
      interceptor.operationsForAllClients(client1, channel, op) returns List(forAllOp, forAllOp)

      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      there was two(transport) !! Event.Msg(client1, channel, forAllMsg)
      there was two(transport) !! Event.Msg(client2, channel, forAllMsg)
      there was one(interceptor).applyOperation(server, client1, channel, op)
      there was two(interceptor).applyOperation(server, client1, channel, forAllOp)
    }

    "return error if interceptor throws an exception on operation applying" in {
      interceptor.applyOperation(server, client1, channel, op) throws new RuntimeException("")
      server !? Event.Msg(client1, channel, msg) must haveClass[Event.Error]
    }

    "return error if interceptor throws an exception when generating operations for creating client" in {
      interceptor.operationsForCreatingClient(client1, channel, op) throws new RuntimeException("")
      server !? Event.Msg(client1, channel, msg) must haveClass[Event.Error]
    }

    "return error if interceptor throws an exception when generating operations for all clients" in {
      interceptor.operationsForAllClients(client1, channel, op) throws new RuntimeException("")
      server !? Event.Msg(client1, channel, msg) must haveClass[Event.Error]
    }
    
    "return error if synchronizer throws exception when adding msg" in {
      synchronizer.receiveRemoteOperation(msg) throws new RuntimeException("")
      server !? Event.Msg(client1, channel, msg) must haveClass[Event.Error]
    }

    "quit all clients from specified channel" in {
      server !? Event.ShutdownChannel(channel, "any reason") must equalTo(Event.Ok())
      server.clients.contains(client1) must equalTo(false)
      server.clients.contains(client2) must equalTo(false)
      server.clients.contains(clientInOtherChannel) must equalTo(true)
    }

    "inform all clients in channel when channel has been shutdown" in {
      server !? Event.ShutdownChannel(channel, "any reason") must equalTo(Event.Ok())
      there was one(transport) !! Event.Msg(client1, channel, ChannelShutdown("any reason"))
      there was one(transport) !! Event.Msg(client2, channel, ChannelShutdown("any reason"))
      there was no(transport) !! Event.Msg(clientInOtherChannel, channel, ChannelShutdown("any reason"))
    }
  }
}
