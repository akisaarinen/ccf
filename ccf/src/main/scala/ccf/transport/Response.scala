package ccf.transport

case class Response(headers: Headers, content: Option[Any]) {
  def header(key: String): Option[String] = headers.headers.get(key)
}
