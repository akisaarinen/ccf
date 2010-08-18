package ccf.transport.http

import java.io.IOException
import java.net.URL

import ccf.transport.Connection
import ccf.transport.json.{JsonEncoder, JsonDecoder}
import ccf.transport.{ConnectionException, InvalidRequestException}
import ccf.transport.{Encoder, Decoder}
import ccf.transport.{Request, Response}

object HttpConnection {
  private val timeoutMillis = 1000
  def create(url: URL) = new HttpConnection(url, new DispatchHttpClient(timeoutMillis), JsonDecoder, JsonEncoder)
}

class HttpConnection(url: URL, client: HttpClient, decoder: Decoder, encoder: Encoder) extends Connection {
  def send(request: Request): Option[Response] = try {
    decoder.decodeResponse(post(request))
  } catch {
    case e: IOException => throw new ConnectionException(e.toString)
  }
  private def post(request: Request) = client.post(requestUrl(request), encoder.encodeRequest(request))
  private def requestUrl(request: Request) = new URL(url, request.header("type").getOrElse(requestTypeMissing))
  private def requestTypeMissing = throw new InvalidRequestException("Request header \"type\" missing")
}
