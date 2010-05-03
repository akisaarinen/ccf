package ccf.transport.json

import com.twitter.json.{Json, JsonException}

object JsonParser extends Parser {
  def parse(message: String): Option[Response] = handleJsonException {
    if (message.isEmpty) None else Some(response(message))
  }
  private def response(message: String): Response = Json.parse(message) match {
    case m: Map[Any, Any] => Response(headers(m), content(m))
    case _                => throw new MalformedDataException("Invalid message frame")
  }
  private def headers(m: Map[Any, Any]): Headers = m.get("headers") match {
    case Some(headers) => Headers(headersToMap(headers))
    case None          => throw new MalformedDataException("Missing message header")
  }
  private def headersToMap(headers: Any) = {
    if (headers.isInstanceOf[Map[Any,Any]]) { headers.asInstanceOf[Map[Any, Any]] }
    else throw new MalformedDataException("Invalid message header")
  }
  private def content(m: Map[Any, Any]): Option[Any] = m.get("content")
  private def handleJsonException(f: => Option[Response]) =
    try { f } catch { case e: JsonException => throw new MalformedDataException(e.toString) }
}
