package ccf.transport.http

import java.io.IOException
import java.net.URL

import ccf.transport.json.{JsonFormatter, JsonDecoder}

object HttpConnection {
  private val timeoutMillis = 1000
  def create(url: URL) = new HttpConnection(url, new DispatchHttpClient(timeoutMillis), JsonDecoder, JsonFormatter)
}

class HttpConnection(url: URL, client: HttpClient, decoder: Decoder, formatter: Formatter) extends Connection {
  def send(request: Request): Option[Response] = try {
    decoder.decodeResponse(post(request))
  } catch {
    case e: IOException => throw new ConnectionException(e.toString)
  }
  private def post(request: Request) = client.post(requestUrl(request), formatter.formatRequest(request))
  private def requestUrl(request: Request) = new URL(url, request.header("type").getOrElse(requestTypeMissing))
  private def requestTypeMissing = throw new InvalidRequestException("Request header \"type\" missing")
}
