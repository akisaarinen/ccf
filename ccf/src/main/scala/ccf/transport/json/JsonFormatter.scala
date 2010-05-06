package ccf.transport.json

import com.twitter.json.Json

object JsonFormatter extends Formatter {
  def formatRequest(r: Request): String = Json.build(toMap(r.headers, r.content)).toString
  def formatResponse(r: Response): String = Json.build(toMap(r.headers, r.content)).toString
  private def toMap(headers: Map[String, String], content: Option[Any]) = content match {
    case Some(c) => Map("headers" -> headers, "content" -> c)
    case None    => Map("headers" -> headers)
  }
}
