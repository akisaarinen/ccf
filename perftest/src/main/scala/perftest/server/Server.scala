package perftest.server

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{Server => Jetty7Server, Request, Connector}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import ccf.transport.Response
import java.net.URL
import ccf.transport.json.{JsonDecoder, JsonEncoder}
import java.io.StringWriter
import ccf.session.AbstractRequest
import ccf.OperationContext
import ccf.tree.operation.{TreeOperationDecoder, TreeOperation}

class HttpRequestHandler extends AbstractHandler {
  override def handle(target: String, req: Request, httpReq: HttpServletRequest, httpResp: HttpServletResponse) {
    var requestBody = readRequestBody(req)
    val request = JsonDecoder.decodeRequest(requestBody)
    val operationDecoder = new TreeOperationDecoder {
      protected def parseModifier(encodedValue: Any) = null
      protected def parseNode(encodedValue: Any) = null
    }

    request match {
      case Some(r: ccf.transport.Request) => {
        r.header("type") match {
          case Some(AbstractRequest.joinRequestType) =>
          case Some(AbstractRequest.partRequestType) =>
          case Some(AbstractRequest.contextRequestType) => {
            val encodedContext = r.content.get.asInstanceOf[Map[String, Any]]
            val op = operationDecoder.decode(encodedContext("op"))
            val localMsgSeqNo = encodedContext("localMsgSeqNo").asInstanceOf[Int]
            val remoteMsgSeqNo = encodedContext("remoteMsgSeqNo").asInstanceOf[Int]
            val context = new OperationContext[TreeOperation](op, localMsgSeqNo, remoteMsgSeqNo)
            println("Decoded context: " + context)
          }
          case Some(unknownRequestType) => error("Unknown request type: " + unknownRequestType)
          case None => error("No request type given")
        }
      }
      case None => error("Unable to decode request")
    }
    (httpReq.asInstanceOf[Request]).setHandled(true)
  }
  private def readRequestBody(request: HttpServletRequest): String = {
    val reader = request.getReader
    val buf = new Array[Char](4096)
    val writer = new StringWriter
    var len = 0
    while ({ len = reader.read(buf, 0, buf.length); len != -1}) {
      writer.write(buf, 0, len)
    }
    writer.toString
  }
  private def writeTestResponse(httpResp: HttpServletResponse) {
    val response = Response(Map[String, String](), Some((0 to 1023).map(x => 0).mkString("")))
    val body = JsonEncoder.encodeResponse(response)
    httpResp.setContentType("application/json")
    httpResp.setStatus(HttpServletResponse.SC_OK);
    httpResp.setContentLength(body.length)
    httpResp.getWriter.write(body)
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
