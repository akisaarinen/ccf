package ccf.server

import ccf.messaging.{ChannelShutdown, ConcurrentOperationMessage}
import ccf.transport.{ClientId, ChannelId, Event}
import ccf.operation.Operation
import ccf.transport.TransportActor

import org.specs.Specification
import org.specs.mock.Mockito
import org.mockito.Matchers._

object ServerSpec extends Specification with Mockito {
  val synchronizer = mock[OperationSynchronizer[Operation]] 
  val transport = mock[TransportActor]
  val factory = mock[OperationSynchronizerFactory[Operation]]
  factory.createSynchronizer returns synchronizer
  val interceptor = mock[ServerOperationInterceptor[Operation]]
  interceptor.operationsForCreatingClient(anyObject[ClientId], anyObject[ChannelId], anyObject[Operation]) returns List()
  interceptor.operationsForAllClients(anyObject[ClientId], anyObject[ChannelId], anyObject[Operation]) returns List()
  val server = new Server(factory, interceptor)

  "Server with no clients" should {
    "accept a client and return current state" in {
      val client = ClientId.randomId
      val channel = ChannelId.randomId
      case class MyStateClass(i: Int)
      val myState = MyStateClass(123)
      interceptor.currentStateFor(channel) returns myState
      server !? Event.Join(transport, client, channel) must equalTo(Event.State(client, channel, myState))
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
      server !? Event.Join(transport, client1, channel)
      server !? Event.Join(transport, client2, channel)
      server !? Event.Join(transport, client3, channel)
    }
    
    "quit a joined client" in {
      server !? Event.Quit(client1, channel) must equalTo(Event.Ok())
      server.clients.contains(client1) must equalTo(false)
    }

    "not quit an unknown client" in {
      server !? Event.Quit(ClientId.randomId, channel) must haveClass[Event.Error]
    }

    "not allow a client to join another channel before quitting" in {
      server !? Event.Join(transport, client1, ChannelId.randomId) must haveClass[Event.Error]
    }

    "return error if interceptor throws an exception" in {
      interceptor.currentStateFor(channel) throws new RuntimeException("")
      server !? Event.Join(transport, client1, channel) must haveClass[Event.Error]
    }
  }

  "Server with registered clients on two channels" should {
    val channel = ChannelId.randomId
    val client1 = ClientId.randomId
    val client2 = ClientId.randomId
    val otherChannel = ChannelId.randomId
    val clientInOtherChannel = ClientId.randomId
    val msg = mock[ConcurrentOperationMessage[Operation]]
    val op = mock[Operation]
    synchronizer.receiveRemoteOperation(msg) returns op
    synchronizer.createLocalOperation(op) returns msg
      
    doBefore {
      server !? Event.Join(transport, client1, channel)
      server !? Event.Join(transport, client2, channel)
      server !? Event.Join(transport, clientInOtherChannel, otherChannel)
    }

    "propagate messages from a client to others in same channel" in {
      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      transport ! Event.Msg(client2, channel, msg) was called
      transport had noMoreCalls
    }

    "not accept message if client has not joined the channel" in {
      server !? Event.Msg(clientInOtherChannel, channel, msg) must haveClass[Event.Error]
    }

    "propagate operations for creating client" in {
      val creationOp = mock[Operation]
      val creationMsg = mock[ConcurrentOperationMessage[Operation]]
      synchronizer.receiveRemoteOperation(creationMsg) returns creationOp
      synchronizer.createLocalOperation(creationOp) returns creationMsg
      interceptor.operationsForCreatingClient(client1, channel, op) returns List(creationOp, creationOp)
      
      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      transport ! Event.Msg(client1, channel, creationMsg) was called.twice
      interceptor.applyOperation(server, client1, channel, op) was called
    }

    "propagate operations for all clients" in {
      val forAllOp = mock[Operation]
      val forAllMsg = mock[ConcurrentOperationMessage[Operation]]
      synchronizer.receiveRemoteOperation(forAllMsg) returns forAllOp
      synchronizer.createLocalOperation(forAllOp) returns forAllMsg
      interceptor.operationsForAllClients(client1, channel, op) returns List(forAllOp, forAllOp)
      
      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      transport ! Event.Msg(client1, channel, forAllMsg) was called.twice
      transport ! Event.Msg(client2, channel, forAllMsg) was called.twice
      interceptor.applyOperation(server, client1, channel, op) was called
      interceptor.applyOperation(server, client1, channel, forAllOp) was called.twice
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

    "quit all clients from specified channel" in {
      server !? Event.ShutdownChannel(channel, "any reason") must equalTo(Event.Ok())
      server.clients.contains(client1) must equalTo(false)
      server.clients.contains(client2) must equalTo(false)
      server.clients.contains(clientInOtherChannel) must equalTo(true)
    }

    "inform all clients in channel when channel has been shutdown" in {
      server !? Event.ShutdownChannel(channel, "any reason") must equalTo(Event.Ok())
      transport ! Event.Msg(client1, channel, ChannelShutdown("any reason")) was called
      transport ! Event.Msg(client2, channel, ChannelShutdown("any reason")) was called
      transport ! Event.Msg(clientInOtherChannel, channel, ChannelShutdown("any reason")) wasnt called
    }
  }
}
