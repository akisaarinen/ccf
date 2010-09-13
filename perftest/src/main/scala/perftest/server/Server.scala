/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package perftest.server

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{Server => Jetty7Server, Request => Jetty7Request, Connector}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.net.URL
import ccf.transport.json.{JsonDecoder, JsonEncoder}
import java.io.StringWriter
import ccf.session.SessionRequest
import ccf.OperationContext
import ccf.tree.operation.{TreeOperationDecoder, TreeOperation}
import ccf.transport.{TransportRequest, TransportResponse}

class HttpRequestHandler(engine: ServerEngine) extends AbstractHandler {
  override def handle(target: String, req: Jetty7Request, httpReq: HttpServletRequest, httpResp: HttpServletResponse) {
    var requestBody = readRequestBody(req)
    val request = JsonDecoder.decodeRequest(requestBody)

    engine.decodeRequest(request)
    (httpReq.asInstanceOf[Jetty7Request]).setHandled(true)
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
    val response = TransportResponse(Map[String, String](), Some((0 to 1023).map(x => 0).mkString("")))
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
    val engine = new ServerEngine

    connector.setPort(url.getPort)
    server.setConnectors(List[Connector](connector).toArray)
    server.setHandler(new HttpRequestHandler(engine))
    server.start
  }
}