package ccf.transport.json

import com.twitter.json.Json

import scala.collection.immutable.TreeMap

object JsonFormatter extends Formatter {
  def format(request: Request): String = Json.build(toMap(request)).toString
  private def toMap(request: Request) = TreeMap[String, Any]("headers" -> request.headers, "content" -> request.content)
}
