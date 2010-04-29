package ccf.transport.json

import com.twitter.json.Json

object JsonParser extends Parser {
  def parse(message: String): Any = {
    try { Json.parse(message) }
    catch { case e: Exception => throw new MalformedDataException(e.toString) }
  }
}
