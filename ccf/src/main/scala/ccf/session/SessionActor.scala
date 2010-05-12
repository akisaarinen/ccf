package ccf.session

import scala.actors.Actor
import ccf.transport.{Connection, ConnectionException}

class SessionActor(connection: Connection, clientId: ClientId, version: Version) extends Actor {
  start
  def act { loop(Session(connection, version, clientId, 0, Set())) }
  private def loop(s: Session) { react { case msg: Message => loop(handleMessage(s, msg)) } }
  private def handleMessage(session: Session, msg: Message): Session = session.send(msg) match {
    case (nextSession: Session, _) => nextSession
  }
}
