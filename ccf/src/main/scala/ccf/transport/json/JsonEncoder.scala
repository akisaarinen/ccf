package ccf.transport.json

import ccf.transport.Encoder
import ccf.transport.{Request, Response}
import com.twitter.json.Json

object JsonEncoder extends Encoder {
  def encodeRequest(r: Request): String = Json.build(toMap(r.headers, r.content)).toString
  def encodeResponse(r: Response): String = Json.build(toMap(r.headers, r.content)).toString
  private def toMap(headers: Map[String, String], content: Option[Any]) = content match {
    case Some(c) => Map("headers" -> headers, "content" -> c)
    case None    => Map("headers" -> headers)
  }
}
