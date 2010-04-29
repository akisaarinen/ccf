package ccf.transport

case class Response(headers: Map[String, String], content: Option[Any])
