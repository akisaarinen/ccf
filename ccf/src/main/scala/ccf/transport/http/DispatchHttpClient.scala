package ccf.transport.http

import org.apache.http.params.HttpConnectionParams

import dispatch.{Http => DispatchHttp}
import dispatch.Http._

import java.net.URL

class DispatchHttpClient(timeoutMillis: Int) extends HttpClient {
  private val http = new DispatchHttp
  init
  def post(url: URL, data: String): String = http(url.toString.POST << data >- { x => x })
  private def init {
    HttpConnectionParams.setConnectionTimeout(httpClientParams, timeoutMillis)
    HttpConnectionParams.setSoTimeout(httpClientParams, timeoutMillis)
  }
  private def httpClientParams = http.client.getParams
}
