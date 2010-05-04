package ccf.transport.json

import com.twitter.json.{Json, JsonException}

object JsonParser extends Parser {
  def parse(message: String): Option[Response] = try { 
    if (message.isEmpty) None else Some(response(message)) 
  } catch { 
    case e: JsonException => throw new MalformedDataException(e.toString)
  }
  private def response(message: String): Response = Json.parse(message) match {
    case m: Map[Any, Any] => Response(headers(m), content(m))
    case _                => malformedDataException("Invalid message frame")
  }
  private def headers(m: Map[Any, Any]): Headers = m.get("headers") match {
    case Some(headers) => Headers(headersToMap(headers))
    case None          => malformedDataException("Missing message header")
  }
  private def headersToMap(headers: Any) = headers match {
    case m: Map[Any, Any] => m
    case _                => malformedDataException("Invalid message header")
  }
  private def content(m: Map[Any, Any]): Option[Any] = m.get("content")
  private def malformedDataException(s: String) = throw new MalformedDataException(s)
}
