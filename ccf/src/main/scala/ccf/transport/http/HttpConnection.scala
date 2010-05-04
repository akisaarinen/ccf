package ccf.transport.http

import java.io.IOException
import java.net.URL

import org.apache.http.params.HttpConnectionParams
import ccf.transport.json.{JsonFormatter, JsonParser}

import dispatch.{Request => HttpRequest, Http}
import dispatch.Http._

class HttpConnection(url: URL, timeoutMillis: Int, http: Http) extends Connection {
  private val formatter = JsonFormatter
  private val parser = JsonParser
  init
  def this(url: URL, timeoutMillis: Int) = this(url, timeoutMillis, new Http)
  def send(request: Request): Option[Response] = {
    val req = requestUrl(request).POST << formatter.format(request)
    http(req >- { parser.parse(_) })
  }
  private def requestUrl(request: Request) = url.toString / request.header("type").getOrElse(requestTypeMissing)
  private def requestTypeMissing = throw new InvalidRequestException("Request header \"type\" missing")
  private def init = {
    HttpConnectionParams.setConnectionTimeout(httpClientParams, timeoutMillis)
    HttpConnectionParams.setSoTimeout(httpClientParams, timeoutMillis)
  }
  private def httpClientParams = http.client.getParams
}
