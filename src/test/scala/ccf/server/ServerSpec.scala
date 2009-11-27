package ccf.server

import ccf.messaging.ConcurrentOperationMessage
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
  interceptor.operationsForCreatingClient(anyObject[Operation]) returns List()
  interceptor.operationsForAllClients(anyObject[Operation]) returns List()
  val server = new Server(transport, factory, interceptor)

  "Server with no clients" should {
    "accept a client" in {
      val client = ClientId.randomId
      val channel = ChannelId.randomId
      server !? Event.Join(client, channel) must equalTo(Event.Ok())
      server.clients.contains(client) must equalTo(true)
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
      server !? Event.Quit(ClientId.randomId, channel) must equalTo(Event.Error())
    }

    "not allow a client to join another channel before quitting" in {
      server !? Event.Join(client1, ChannelId.randomId) must equalTo(Event.Error())
    }
  }

  "Server with registered clients on two channels" should {
    val channel = ChannelId.randomId
    val client1 = ClientId.randomId
    val client2 = ClientId.randomId
    val clientInOtherChannel = ClientId.randomId
    val msg = mock[ConcurrentOperationMessage[Operation]]
    val op = mock[Operation]
    synchronizer.receiveRemoteOperation(msg) returns op
    synchronizer.createLocalOperation(op) returns msg
      
    doBefore {
      server !? Event.Join(client1, channel)
      server !? Event.Join(client2, channel)
      server !? Event.Join(clientInOtherChannel, ChannelId.randomId)
    }

    "propagate messages from a client to others in same channel" in {
      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      transport ! Event.Msg(client2, channel, msg) was called
      transport had noMoreCalls
    }

    "not accept message if client has not joined the channel" in {
      server !? Event.Msg(clientInOtherChannel, channel, msg) must equalTo(Event.Error())
    }

    "propagate operations for creating client" in {
      val creationOp = mock[Operation]
      val creationMsg = mock[ConcurrentOperationMessage[Operation]]
      synchronizer.receiveRemoteOperation(creationMsg) returns creationOp
      synchronizer.createLocalOperation(creationOp) returns creationMsg
      interceptor.operationsForCreatingClient(op) returns List(creationOp, creationOp)
      
      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      transport ! Event.Msg(client1, channel, creationMsg) was called.twice
      interceptor.applyOperation(op) was called
    }

    "propagate operations for all clients" in {
      val forAllOp = mock[Operation]
      val forAllMsg = mock[ConcurrentOperationMessage[Operation]]
      synchronizer.receiveRemoteOperation(forAllMsg) returns forAllOp
      synchronizer.createLocalOperation(forAllOp) returns forAllMsg
      interceptor.operationsForAllClients(op) returns List(forAllOp, forAllOp)
      
      server !? Event.Msg(client1, channel, msg) must equalTo(Event.Ok())
      transport ! Event.Msg(client1, channel, forAllMsg) was called.twice
      transport ! Event.Msg(client2, channel, forAllMsg) was called.twice
      interceptor.applyOperation(op) was called
      interceptor.applyOperation(forAllOp) was called.twice
    }
  }
}
