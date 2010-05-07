package ccf.transport.http

import java.io.IOException
import java.net.URL

trait HttpClient {
  @throws(classOf[IOException])
  def post(url: URL, data: String): String
}
