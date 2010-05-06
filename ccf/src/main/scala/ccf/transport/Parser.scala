package ccf.transport

trait Parser {
  @throws(classOf[MalformedDataException])
  def parseResponse(msg: String): Option[Response]
}
