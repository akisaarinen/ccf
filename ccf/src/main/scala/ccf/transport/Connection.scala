package ccf.transport

trait Connection {
  @throws(classOf[ConnectionException])
  def invoke(method: String, args: String): String
  @throws(classOf[ConnectionException])
  def disconnect
}
