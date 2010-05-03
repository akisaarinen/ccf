package ccf.transport

trait Connection {
  @throws(classOf[InvalidRequestException])
  @throws(classOf[MalformedDataException])
  @throws(classOf[ConnectionException])
  def send(request: Request): Option[Response]
  @throws(classOf[ConnectionException])
  def disconnect
}
