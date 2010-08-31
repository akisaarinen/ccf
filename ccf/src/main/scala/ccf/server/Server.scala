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

import ccf.messaging.{ChannelShutdown, ConcurrentOperationMessage, Message}
import ccf.transport.{TransportActor, ClientId, ChannelId, Event}
import java.io.{StringWriter, PrintWriter}
import scala.actors.Actor
import scala.actors.Actor._
import collection.mutable.HashMap
import ccf.OperationSynchronizerFactory
import ccf.tree.operation.TreeOperation

trait ServerOperationInterceptor {
  def currentStateFor(channelId: ChannelId): Any
  def applyOperation(server: Server, clientId: ClientId, channelId: ChannelId, op: TreeOperation): Unit
  def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation]
  def operationsForAllClients(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation]
}

class Server(factory: OperationSynchronizerFactory,
                             interceptor: ServerOperationInterceptor,
                             transport: TransportActor) extends Actor {
  val clients = new HashMap[ClientId, ClientState]
  transport.initialize(this)
  start

  def act = loop { react {
    case Event.Join(clientId, channelId) => clients.get(clientId) match {
      case Some(state) if (state.channel != channelId) => reply(Event.Error("Already joined to another channel"))
      case Some(state) => println("already joined to this channel"); reply(getCurrentState(clientId, channelId))
      case None => reply(onJoin(clientId, channelId))
    }
    case Event.Quit(clientId, channelId) => clients.get(clientId) match {
      case None => reply(Event.Error("Not joined, unable to quit"))
      case Some(state) if (state.channel != channelId) => reply(Event.Error("Not in that channel"))
      case Some(state) => reply(onQuit(clientId, channelId))
    }
    case Event.ShutdownChannel(channelId, reason) => reply(onShutdown(channelId, reason))
    case Event.Msg(clientId, channelId, msg) => clients.get(clientId) match {
      case None => reply(Event.Error("Not joined to any channel"))
      case Some(state) if (state.channel != channelId) => reply(Event.Error("Joined to different channel"))
      case Some(state) => reply(onMsg(clientId, channelId, msg.asInstanceOf[Message], state))
    }
    case m => reply(Event.Error("Unknown message %s".format(m)))
  }}

  private def onJoin(clientId: ClientId, channelId: ChannelId): Any = {
    val synchronizer = factory.createSynchronizer
    clients(clientId) = new ClientState(channelId, synchronizer)
    getCurrentState(clientId, channelId)
  }

  private def getCurrentState(clientId: ClientId, channelId: ChannelId): Any = {
    try {
      Event.State(clientId, channelId, interceptor.currentStateFor(channelId))
    } catch {
      case e => Event.Error("Could not get current state: "+stackTraceToString(e))
    }
  }


  private def onQuit(clientId: ClientId, channelId: ChannelId): Any = {
    clients -= clientId
    Event.Ok()
  }

  private def onMsg(clientId: ClientId, channelId: ChannelId, msg: Message, state: ClientState): Any = {
    try {
      val op = state.receive(msg.asInstanceOf[ConcurrentOperationMessage])
      interceptor.applyOperation(this, clientId, channelId, op)

      val others = otherClientsFor(clientId)
      others.foreach { otherClientId =>
        val msgForOther = clients(otherClientId).send(op)
        transport !! Event.Msg(otherClientId, channelId, msgForOther)
      }

      val opsForCreator = interceptor.operationsForCreatingClient(clientId, channelId, op)
      opsForCreator.foreach { opForCreator =>
        val msgForCreator = clients(clientId).send(opForCreator)
        transport !! Event.Msg(clientId, channelId, msgForCreator)
      }

      val opsForAll = interceptor.operationsForAllClients(clientId, channelId, op)
      opsForAll.foreach { opForAll =>
        interceptor.applyOperation(this, clientId, channelId, opForAll)
        clientsForChannel(channelId).foreach { clientInChannel =>
          val msgForClient = clients(clientInChannel).send(opForAll)
          transport !! Event.Msg(clientInChannel, channelId, msgForClient)
        }
      }

      Event.Ok()
    } catch {
      case e => Event.Error(stackTraceToString(e))
    }
  }

  private def onShutdown(channelId: ChannelId, reason: String): Any = {
    val shutdownMsg = ChannelShutdown(reason)
    clientsForChannel(channelId).foreach { clientId =>
      clients.get(clientId).foreach { state =>
        transport !! Event.Msg(clientId, channelId, shutdownMsg)
      }
      clients -= clientId
    }
    Event.Ok()
  }

  private def clientsForChannel(channelId: ChannelId): List[ClientId] = {
    clients filter { case (id, state) => state.channel == channelId } map { case (id, _) => id } toList
  }

  private def otherClientsFor(clientId: ClientId): List[ClientId] = {
    clients.get(clientId) match {
      case Some(state) => clientsForChannel(state.channel).filter(_ != clientId)
      case None => List()
    }
  }

  private def stackTraceToString(e: Throwable): String = {
    val result = new StringWriter
    val writer = new PrintWriter(result)
    e.printStackTrace(writer)
    result.toString
  }
}
