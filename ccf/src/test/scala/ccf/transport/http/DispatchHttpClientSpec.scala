package ccf.transport.http

import org.specs.Specification
import org.apache.http.conn.scheme.{Scheme, SocketFactory}
import org.specs.mock.Mockito

class DispatchHttpClientSpec extends Specification with Mockito {
  "Dispatch http client" should {
    "register scheme" in {
      val scheme = new Scheme("https", mock[SocketFactory], 443)
      val dispatchClient = new DispatchHttpClient(1000, Some(scheme))
      dispatchClient.getConnectionManager.getSchemeRegistry.get("https") must equalTo(scheme)
    }
  }
}