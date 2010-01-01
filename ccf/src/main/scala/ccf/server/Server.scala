package ccf.server

import ccf.messaging.{ChannelShutdown, ConcurrentOperationMessage}
import ccf.operation.Operation
import ccf.transport.{TransportActor, ClientId, ChannelId, Event}
import ccf.tree.JupiterTreeTransformation
import ccf.tree.indexing.TreeIndex
import ccf.tree.operation._

import java.io.{StringWriter, PrintWriter}
import scala.actors.Actor
import scala.actors.Actor._
import collection.mutable.HashMap

trait ServerOperationInterceptor[T <: Operation] {
  def currentStateFor(channelId: ChannelId): Any
  def applyOperation(server: Server[T], clientId: ClientId, channelId: ChannelId, op: T): Unit
  def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: T): List[T]
  def operationsForAllClients(clientId: ClientId, channelId: ChannelId, op: T): List[T]
}

class Server[T <: Operation](factory: OperationSynchronizerFactory[T],
                             interceptor: ServerOperationInterceptor[T],
                             transport: TransportActor) extends Actor {
  val clients = new HashMap[ClientId, ClientState[T]]
  transport.initialize(this)
  start

  def act = loop { react {
    case Event.Join(clientId, channelId) => clients.get(clientId) match {
      case Some(state) => reply(Event.Error("Already joined"))
      case None => reply(onJoin(clientId, channelId))
    }
    case Event.Quit(clientId, channelId) => clients.get(clientId) match {
      case None => reply(Event.Error("Not joined, unable to quit"))
      case Some(state) if (state.channel != channelId) => reply(Event.Error("Not in that channel"))
      case Some(state) => reply(onQuit(clientId, channelId))
    }
    case Event.ShutdownChannel(channelId, reason) => {
      val shutdownMsg = ChannelShutdown[T](reason)
      clientsForChannel(channelId).foreach { clientId =>
        clients.get(clientId).foreach { state =>
          transport !! Event.Msg(clientId, channelId, shutdownMsg)
        }
        clients -= clientId
      }
      reply(Event.Ok())
    }
    case Event.Msg(clientId, channelId, msg) => clients.get(clientId) match {
      case None => reply(Event.Error("Not joined to any channel"))
      case Some(state) if (state.channel != channelId) => reply(Event.Error("Joined to different channel"))
      case Some(state) => {
        val op = state.receive(msg.asInstanceOf[ConcurrentOperationMessage[T]])
        try {
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

          reply(Event.Ok())
        } catch {
          case e => reply(Event.Error(stackTraceToString(e)))
        }
      }
    }
    case m => reply(Event.Error("Unknown message %s".format(m)))
  }}

  private def onJoin(clientId: ClientId, channelId: ChannelId): Any = {
    val synchronizer = factory.createSynchronizer
    clients(clientId) = new ClientState(channelId, synchronizer)
    try {
      val currentState = interceptor.currentStateFor(channelId)
      Event.State(clientId, channelId, currentState)
    } catch {
      case e => Event.Error(stackTraceToString(e))
    }
  }

  private def onQuit(clientId: ClientId, channelId: ChannelId): Any = {
    clients -= clientId
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
