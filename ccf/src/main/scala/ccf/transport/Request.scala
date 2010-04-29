package ccf.transport

case class Request(headers: Map[String, String], content: Option[Any])
