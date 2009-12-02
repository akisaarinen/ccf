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
  def applyOperation(op: T): Unit
  def operationsForCreatingClient(op: T): List[T]
  def operationsForAllClients(op: T): List[T]
}

class Server[T <: Operation](factory: OperationSynchronizerFactory[T],
                             interceptor: ServerOperationInterceptor[T]) extends TransportActor {
  val clients = new HashMap[ClientId, ClientState[T]]
  start

  def act = loop { react {
    case Event.Join(clientId, channelId) => clients.get(clientId) match {
      case Some(state) => reply(Event.Error())
      case None => {
        val synchronizer = factory.createSynchronizer
        clients(clientId) = new ClientState(channelId, synchronizer)
        val currentState = interceptor.currentStateFor(channelId)
        reply(Event.State(clientId, channelId, currentState))
      }
    }
    case Event.Quit(clientId, channelId) => clients.get(clientId) match {
      case None => reply(Event.Error())
      case Some(state) if (state.channel != channelId) => reply(Event.Error())
      case Some(state) => {
        clients -= clientId
        reply(Event.Ok())
      }
    }
    case Event.Msg(transport, clientId, channelId, msg) => clients.get(clientId) match {
      case None => reply(Event.Error())
      case Some(state) if (state.channel != channelId) => reply(Event.Error())
      case Some(state) => {
        val op = state.receive(msg.asInstanceOf[ConcurrentOperationMessage[T]])
        interceptor.applyOperation(op)
        
        val others = otherClientsFor(clientId)
        others.foreach { otherClientId => 
          val msgForOther = clients(otherClientId).send(op)   
          transport ! Event.Msg(this, otherClientId, channelId, msgForOther)
        }

        val opsForCreator = interceptor.operationsForCreatingClient(op)
        opsForCreator.foreach { opForCreator => 
          val msgForCreator = clients(clientId).send(opForCreator)
          transport ! Event.Msg(this, clientId, channelId, msgForCreator)
        }

        val opsForAll = interceptor.operationsForAllClients(op)
        opsForAll.foreach { opForAll => 
          interceptor.applyOperation(opForAll)
          clientsForChannel(channelId).foreach { clientInChannel =>
            val msgForClient = clients(clientInChannel).send(opForAll)
            transport ! Event.Msg(this, clientInChannel, channelId, msgForClient)
          }
        }

        reply(Event.Ok())
      }
    }
    case _ => reply(Event.Error())
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
