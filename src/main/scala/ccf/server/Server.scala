package ccf.server

import ccf.messaging.ConcurrentOperationMessage
import ccf.operation.Operation
import ccf.transport.{TransportActor, ClientId, ChannelId, Event}
import ccf.tree.JupiterTreeTransformation
import ccf.tree.indexing.TreeIndex
import ccf.tree.operation._

import scala.actors.Actor._
import collection.mutable.HashMap

trait ServerOperationInterceptor[T <: Operation] {
  def currentStateFor(channelId: ChannelId): Any
  def applyOperation(clientId: ClientId, channelId: ChannelId, op: T): Unit
  def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: T): List[T]
  def operationsForAllClients(clientId: ClientId, channelId: ChannelId, op: T): List[T]
}

class Server[T <: Operation](factory: OperationSynchronizerFactory[T],
                             interceptor: ServerOperationInterceptor[T]) extends TransportActor {
  val clients = new HashMap[ClientId, ClientState[T]]
  start

  def act = loop { react {
    case Event.Join(clientId, channelId) => clients.get(clientId) match {
      case Some(state) => reply(Event.Error("Already joined"))
      case None => {
        val synchronizer = factory.createSynchronizer
        clients(clientId) = new ClientState(channelId, synchronizer)
        try {
          val currentState = interceptor.currentStateFor(channelId)
          reply(Event.State(clientId, channelId, currentState))
        } catch {
          case e => reply(Event.Error(e.toString))
        }
      }
    }
    case Event.Quit(clientId, channelId) => clients.get(clientId) match {
      case None => reply(Event.Error("Not joined, unable to quit"))
      case Some(state) if (state.channel != channelId) => reply(Event.Error("Not in that channel"))
      case Some(state) => {
        clients -= clientId
        reply(Event.Ok())
      }
    }
    case Event.Msg(transport, clientId, channelId, msg) => clients.get(clientId) match {
      case None => reply(Event.Error("Not joined to any channel"))
      case Some(state) if (state.channel != channelId) => reply(Event.Error("Joined to different channel"))
      case Some(state) => {
        val op = state.receive(msg.asInstanceOf[ConcurrentOperationMessage[T]])
        try {
          interceptor.applyOperation(clientId, channelId, op)

          val others = otherClientsFor(clientId)
          others.foreach { otherClientId =>
            val msgForOther = clients(otherClientId).send(op)
            transport ! Event.Msg(this, otherClientId, channelId, msgForOther)
          }

          val opsForCreator = interceptor.operationsForCreatingClient(clientId, channelId, op)
          opsForCreator.foreach { opForCreator =>
            val msgForCreator = clients(clientId).send(opForCreator)
            transport ! Event.Msg(this, clientId, channelId, msgForCreator)
          }

          val opsForAll = interceptor.operationsForAllClients(clientId, channelId, op)
          opsForAll.foreach { opForAll =>
            interceptor.applyOperation(clientId, channelId, opForAll)
            clientsForChannel(channelId).foreach { clientInChannel =>
              val msgForClient = clients(clientInChannel).send(opForAll)
              transport ! Event.Msg(this, clientInChannel, channelId, msgForClient)
            }
          }

          reply(Event.Ok())
        } catch {
          case e => reply(Event.Error(e.toString))
        }
      }
    }
  }}

  private def clientsForChannel(channelId: ChannelId): List[ClientId] = {
    clients filter { case (id, state) => state.channel == channelId } map { case (id, _) => id } toList
  }

  private def otherClientsFor(clientId: ClientId): List[ClientId] = {
    clients.get(clientId) match {
      case Some(state) => clientsForChannel(state.channel).filter(_ != clientId)
      case None => List()
    }
  }
}
