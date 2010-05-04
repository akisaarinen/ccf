package ccf.transport.http

import org.specs.Specification
import java.net.URL

object HttpConnectionSpec extends Specification {
  "Invalid request" should {
    "cause an InvalidRequestException" in {
      val conn = new HttpConnection(new URL("http://www.com"), 1000)
      conn.send(new Request(Headers(), None)) must throwA[InvalidRequestException]
    }
  }
}
