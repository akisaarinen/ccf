package ccf.session

import ccf.transport.{Request, Response}

trait Message {
  def send(s: Session): (Session, Option[Response])
  protected def sendRequest(request: Session => Request, session: Session): Option[Response] = session.connection.send(request(session))
}
