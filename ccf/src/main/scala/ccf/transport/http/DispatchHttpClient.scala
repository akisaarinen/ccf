package ccf.transport.http

import org.apache.http.params.HttpConnectionParams

import dispatch.{Http => DispatchHttp}
import dispatch.Http._

import java.net.URL

class DispatchHttpClient(timeoutMillis: Int) extends HttpClient {
  private val http = new DispatchHttp
  init
  def post[T](url: URL, data: String)(block: String => T): T = http(url.toString.POST << data >- { block })
  private def init {
    HttpConnectionParams.setConnectionTimeout(httpClientParams, timeoutMillis)
    HttpConnectionParams.setSoTimeout(httpClientParams, timeoutMillis)
  }
  private def httpClientParams = http.client.getParams
}
