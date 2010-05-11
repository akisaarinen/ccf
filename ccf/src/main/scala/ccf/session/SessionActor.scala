package ccf.session

import scala.actors.Actor
import ccf.transport.Connection

class SessionActor(connection: Connection, clientId: ClientId, version: Version) extends Actor {
  start
  def act { loop(Session(connection, version, clientId, 0, Set())) }
  private def loop(s: Session) { react { case msg: Message => loop(s.send(msg)) } }
}
