package ccf.transport

case class Request(headers: Headers, content: Option[Any]) {
  def header(key: String): Option[String] = headers.headers.get(key)
}

