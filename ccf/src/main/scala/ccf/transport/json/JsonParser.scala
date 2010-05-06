package ccf.transport.json

import com.twitter.json.{Json, JsonException}

object JsonParser extends Parser {
  def parseResponse(m: String) = handleMessage[Response](m, toResponse)
  def parseRequest(m: String) = handleMessage[Request](m, toRequest)
  private def handleMessage[T](m: String, f: (Map[String, String], Option[Any]) => T) = {
    try { if (m.isEmpty) None else { Some(parse(m, f)) } }
    catch { case e: JsonException => malformedDataException(e.toString) }
  }
  private def parse[T](msg: String, f: (Map[String, String], Option[Any]) => T) = Json.parse(msg) match {
    case m: Map[Any, Any] => f(headers(m), content(m))
    case _                => malformedDataException("Invalid message frame")
  }
  private def headers(m: Map[Any, Any]): Map[String, String] = m.get("headers") match {
    case Some(headers) => headersToMap(headers)
    case None          => malformedDataException("Missing message header")
  }
  private def headersToMap(headers: Any): Map[String, String] = headers match {
    case m: Map[Any, Any] => {
      val seqOfHeaders = for ((k, v) <- m) yield (k.toString, v.toString)
      Map[String, String](seqOfHeaders.toList: _*)
    }
    case _                => malformedDataException("Invalid message header")
  }
  private def content(m: Map[Any, Any]): Option[Any] = m.get("content")
  private def toResponse(h: Map[String, String], c: Option[Any]) = Response(h, c)
  private def toRequest(h: Map[String, String], c: Option[Any]) = Request(h, c)
  private def malformedDataException(s: String) = throw new MalformedDataException(s)
}
