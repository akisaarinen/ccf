package ccf.transport

trait Decoder {
  @throws(classOf[MalformedDataException])
  def decodeResponse(msg: String): Option[Response]
  @throws(classOf[MalformedDataException])
  def decodeRequest(msg: String): Option[Request]
}
