package ccf.session

import ccf.transport.{Connection, Request, Response, ConnectionException}

case class Session(connection: Connection, version: Version, clientId: ClientId, seqId: Int, channels: Set[ChannelId]) {
  def next(channels: Set[ChannelId]) = Session(connection, version, clientId, seqId + 1, channels)
  def send(msg: Message): (Session, Either[Failure, Success]) = handleException(msg) { msg.send(this) match {
    case (session: Session, Some(response)) => success(session, msg, response.content)
    case (session: Session, None)           => success(session, msg, None)
    case _                                  => error("Session got invalid response from #send")
  }}
  private def handleException(msg: Message)(block: => (Session, Either[Failure, Success])) =
    try { block } catch { case e: Exception => failure(msg, e.toString) }
  private def success(s: Session, m: Message, content: Option[Any]) = (s, Right(Success(m, content)))
  private def failure(msg: Message, reason: String) = (this, Left(Failure(msg, reason)))
}
