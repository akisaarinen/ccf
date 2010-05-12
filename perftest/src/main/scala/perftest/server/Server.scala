package perftest.server

import java.net.InetSocketAddress

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{Server => Jetty7Server, Request, Handler, Connector}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import ccf.transport.Response
import ccf.transport.json.JsonFormatter

import java.net.URL

class HttpRequestHandler extends AbstractHandler {
  override def handle(target: String, req: Request, httpReq: HttpServletRequest, httpResp: HttpServletResponse) { 
    val response = Response(Map[String, String](), Some((0 to 1023).map(x => 0).mkString("")))
    val body = JsonFormatter.formatResponse(response)
    httpResp.setContentType("application/json")
    httpResp.setStatus(HttpServletResponse.SC_OK);
    httpResp.setContentLength(body.length)
    httpResp.getWriter.write(body)
    (httpReq.asInstanceOf[Request]).setHandled(true);
  }
}

object Server {
  def run(url: URL)= { 
    val server = new Jetty7Server(url.getPort)
    val connector = new SelectChannelConnector()
    connector.setPort(url.getPort)
    server.setConnectors(List[Connector](connector).toArray)
    server.setHandler(new HttpRequestHandler())
    server.start
  }
}
