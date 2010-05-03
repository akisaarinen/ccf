package ccf.transport.http

import java.io.IOException
import java.net.URL

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{DefaultHttpClient, BasicResponseHandler}
import org.apache.http.params.HttpConnectionParams

class HttpConnection(url: URL, timeoutMillis: Int) extends Connection {
  private val httpClient = new DefaultHttpClient
  init
  def invoke(method: String, args: String): String = {
    val httpPost = new HttpPost(List(url.toString, method).mkString("/")) {
      setEntity(new StringEntity(args))
    }
    try { httpClient.execute[String](httpPost, new BasicResponseHandler) } 
    catch { case e: IOException => throw new ConnectionException(e.toString) }
  }
  def send(request: Request): Option[Response] = {
    error("Not implemented")
  }
  def disconnect = httpClient.getConnectionManager.shutdown
  private def init = {
    HttpConnectionParams.setConnectionTimeout(httpClientParams, timeoutMillis)
    HttpConnectionParams.setSoTimeout(httpClientParams, timeoutMillis)
  }
  private def httpClientParams = httpClient.getParams
}
