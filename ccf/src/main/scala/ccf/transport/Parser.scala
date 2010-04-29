package ccf.transport

trait Parser {
  @throws(classOf[MalformedDataException])
  def parse(msg: String): Any
}
