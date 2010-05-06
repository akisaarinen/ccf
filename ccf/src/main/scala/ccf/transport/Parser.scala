package ccf.transport

trait Parser {
  @throws(classOf[MalformedDataException])
  def parseResponse(msg: String): Option[Response]
  @throws(classOf[MalformedDataException])
  def parseRequest(msg: String): Option[Request]
}
