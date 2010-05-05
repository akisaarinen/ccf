package ccf.transport.http

import java.io.IOException
import java.net.URL

import ccf.transport.json.{JsonFormatter, JsonParser}

class HttpConnection(url: URL, timeoutMillis: Int, http: Http) extends Connection {
  private val formatter = JsonFormatter
  private val parser = JsonParser
  def this(url: URL, timeoutMillis: Int) = this(url, timeoutMillis, new HttpImpl(timeoutMillis))
  def send(request: Request): Option[Response] = try {
    http.post(requestUrl(request), formatter.format(request)) { parser.parse }
  } catch {
    case e: IOException => throw new ConnectionException(e.toString)
  }
  private def requestUrl(request: Request) = new URL(url, request.header("type").getOrElse(requestTypeMissing))
  private def requestTypeMissing = throw new InvalidRequestException("Request header \"type\" missing")
}
