package ccf.transport.http

import org.specs.Specification
import org.specs.mock.Mockito

import java.io.IOException
import java.net.URL

object HttpConnectionSpec extends Specification with Mockito {
  "Invalid request" should {
    val url = new URL("http://www.url")
    "cause an InvalidRequestException" in {
      val client = mock[HttpClient]
      val parser = mock[Decoder]
      val formatter = mock[Encoder]
      val conn = new HttpConnection(url, client, parser, formatter) 
      conn.send(new Request(Map[String, String](), None)) must throwA[InvalidRequestException]
    }
  }
  "IOException thrown from HttpClient#post" should {
    val requestData = "data"
    val spec = "spec"
    val url = new URL("http://www.url")
    "cause a ConnectionException" in {
      val request = mock[Request]
      request.header("type") returns Some(spec)
      val formatter = mock[Encoder]
      formatter.encodeRequest(request) returns requestData
      val client = mock[HttpClient]
      client.post(new URL(url, spec), requestData) throws new IOException
      val conn = new HttpConnection(url, client, mock[Decoder], formatter)
      conn.send(request) must throwA[ConnectionException]
    }
  }
}
