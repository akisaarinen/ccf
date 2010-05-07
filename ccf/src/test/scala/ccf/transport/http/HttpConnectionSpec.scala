package ccf.transport.http

import org.specs.Specification
import org.specs.mock.Mockito

import java.net.URL

object HttpConnectionSpec extends Specification with Mockito {
  "Invalid request" should {
    val url = new URL("http://www.url")
    "cause an InvalidRequestException" in {
      val client = mock[HttpClient]
      val parser = mock[Parser]
      val formatter = mock[Formatter]
      val conn = new HttpConnection(url, client, parser, formatter) 
      conn.send(new Request(Map[String, String](), None)) must throwA[InvalidRequestException]
    }
  }
}
