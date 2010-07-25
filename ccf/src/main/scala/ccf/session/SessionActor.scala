package ccf.session

import scala.actors.Actor
import ccf.transport.Connection

class SessionActor(connection: Connection, clientId: ClientId, version: Version, session: Session) extends Actor {
  start
  def this(connection: Connection, clientId: ClientId, version: Version) = {
    this(connection, clientId, version, Session(connection, version, clientId, 0, Set()))
  }
  def act { loop(session) }
  private def loop(s: Session) { react {
    case msg: Message => {
      val nextSession = handleMessage(s, msg)
      loop(nextSession)
    }
    case Shutdown => exit
  }}
  private def handleMessage(session: Session, msg: Message): Session = session.send(msg) match {
    case (nextSession: Session, result: Either[Failure, Success]) => { sender ! result; nextSession }
  }
}
