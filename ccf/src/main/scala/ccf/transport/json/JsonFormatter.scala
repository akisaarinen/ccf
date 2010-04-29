package ccf.transport.json

import com.twitter.json.Json

object JsonFormatter extends Formatter {
  def format(data: Any): String = Json.build(data).toString
}
