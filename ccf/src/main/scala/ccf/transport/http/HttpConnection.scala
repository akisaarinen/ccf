package ccf.transport.http

import java.io.IOException
import java.net.URL

import ccf.transport.json.{JsonFormatter, JsonParser}

object HttpConnection {
  private val timeoutMillis = 1000
  def create(url: URL) = new HttpConnection(url, new DispatchHttpClient(timeoutMillis), JsonParser, JsonFormatter)
}

class HttpConnection(url: URL, client: HttpClient, parser: Parser, formatter: Formatter) extends Connection {
  def send(request: Request): Option[Response] = try {
    client.post(requestUrl(request), formatter.formatRequest(request)) { parser.parseResponse }
  } catch {
    case e: IOException => throw new ConnectionException(e.toString)
  }
  private def requestUrl(request: Request) = new URL(url, request.header("type").getOrElse(requestTypeMissing))
  private def requestTypeMissing = throw new InvalidRequestException("Request header \"type\" missing")
}
