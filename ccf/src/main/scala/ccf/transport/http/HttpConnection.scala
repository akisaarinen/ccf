package ccf.transport.http

import java.io.IOException
import java.net.URL

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{DefaultHttpClient, BasicResponseHandler}
import org.apache.http.params.HttpConnectionParams

import ccf.transport.json.{JsonFormatter, JsonParser}

import dispatch.{Request => HttpRequest, _}
import Http._

import scala.collection.immutable.TreeMap

class HttpConnection(url: URL, timeoutMillis: Int) extends Connection {
  private val formatter = JsonFormatter
  private val parser = JsonParser
  private val http = new Http
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
    val req = requestUrl(request).POST << formatter.format(request)
    http(req >- { parser.parse(_) })
  }
  def disconnect = httpClient.getConnectionManager.shutdown
  private def requestUrl(request: Request) = url.toString / request.header("type").getOrElse(requestTypeMissing)
  private def requestTypeMissing = throw new InvalidRequestException("Request header \"type\" missing")
  private def init = {
    HttpConnectionParams.setConnectionTimeout(httpClientParams, timeoutMillis)
    HttpConnectionParams.setSoTimeout(httpClientParams, timeoutMillis)
  }
  private def httpClientParams = httpClient.getParams
}
