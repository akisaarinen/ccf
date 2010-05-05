package ccf.transport.http

import org.specs.Specification
import java.net.URL

object HttpConnectionSpec extends Specification {
  "Invalid request" should {
    "cause an InvalidRequestException" in {
      val conn = HttpConnection.create(new URL("http://www.com"))
      conn.send(new Request(Map[String, String](), None)) must throwA[InvalidRequestException]
    }
  }
}
