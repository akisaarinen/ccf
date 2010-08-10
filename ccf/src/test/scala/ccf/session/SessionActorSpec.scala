package ccf.session

import ccf.transport.{Connection, Response, ConnectionException}
import org.specs.Specification
import org.specs.mock.Mockito

object SessionActorSpec extends Specification with Mockito {
  val connection = mock[Connection]
  val clientId = ClientId.randomId
  val version = Version(1, 2)
  val channelId = ChannelId.randomId
  "SessionActor on Join" should {
    val session = newSession(0, Set())
    val joinMessage = Join(channelId)
    val joinRequest = new JoinRequest().create(session, channelId)
    val sa = new SessionActor(connection, clientId, version)
    "reply with Success(...) when server returns valid response request" in {
      connection.send(joinRequest) returns Some(Response(joinRequest.headers, None))
      sa !? joinMessage must equalTo(Right(Success(joinMessage, None)))
    }
    "reply with Success(...) when server returns no response to request" in {
      connection.send(joinRequest) returns None
      sa !? joinMessage must equalTo(Right(Success(joinMessage, None)))
    }
    "update list of channels in session" in {
      connection.send(joinRequest) returns None
      sa !? joinMessage
      val currentSession = (sa !? Shutdown).asInstanceOf[Session]
      currentSession.seqId must equalTo(1)
      currentSession.channels must contain(channelId)
    }
  }

  "SessionActor on Part" should {
    val session = newSession(1, Set(channelId))
    val partMessage = Part(channelId)
    val partRequest = new PartRequest().create(session, channelId)
    val sa = new SessionActor(connection, clientId, version, session)
    "reply with Success(...) when server returns valid response to request" in {
      connection.send(partRequest) returns Some(Response(partRequest.headers, None))
      sa !? partMessage must equalTo(Right(Success(partMessage, None)))
    }
    "reply with Success(...) when server returns no response to request" in {
      connection.send(partRequest) returns None
      sa !? partMessage must equalTo(Right(Success(partMessage, None)))
    }
    "update list of channels in session" in {
      connection.send(partRequest) returns None
      sa !? partMessage
      val currentSession = (sa !? Shutdown).asInstanceOf[Session]
      currentSession.channels must notContain(channelId)
    }
  }
  
  "SessionActor on connection failure" should {
    val session = newSession(0, Set())
    val message = Join(channelId)
    "reply with Failure(...) when message send fails" in {
      val sa = new SessionActor(connection, clientId, version)
      doThrow(new ConnectionException("Error")).when(connection).send(new JoinRequest().create(session, channelId))
      sa !? message must equalTo(Left(Failure(message, "ccf.transport.ConnectionException: Error")))
    }
  }
  "SessionActor on InChannelMsg" should {
    val session = newSession(1, Set(channelId))
    val content = Some(Map("a" -> "b", "c" -> Map("d" -> 3)))
    val requestType = "app/custom"
    val inChannelMessage = InChannelMessage(requestType, channelId, content)
    val inChannelRequest = new InChannelRequest().create(session, requestType, channelId, content)
    "reply with Success(...) when server returns valid response to request" in {
      val sa = new SessionActor(connection, clientId, version, session)
      connection.send(inChannelRequest) returns Some(Response(inChannelRequest.headers, content))
      sa !? inChannelMessage must equalTo(Right(Success(inChannelMessage, content)))
    }
  }

  private def newSession(seqId: Int, channels: Set[ChannelId]) = Session(connection, version, clientId, seqId, channels)
}
