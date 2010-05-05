package ccf.transport

case class Request(headers: Map[String, String], content: Option[Any]) {
  def header(key: String): Option[String] = headers.get(key)
}

