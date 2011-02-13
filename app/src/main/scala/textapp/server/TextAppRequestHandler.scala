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

package textapp.server

import com.sun.net.httpserver.{HttpHandler, HttpExchange}
import ccf.session.ChannelId
import ccf.server.ServerEngine
import ccf.transport.json.JsonCodec
import io.Source
import textapp.document.{TextAppOperationDecoder, TextDocument}

class TextAppRequestHandler extends HttpHandler {
  private val page404 = 
    <html>
      <head><title>404 - Page not found</title></head>
      <body>
        <h1>404 - Page not found</h1>
      </body>
    </html>

  private val page500 = 
    <html>
      <head><title>500 - Internal server error</title></head>
      <body>
        <h1>500 - Internal server error</h1>
      </body>
    </html>

  private val document = new TextDocument("")
  private val interceptor = new TextAppOperationInterceptor(document)
  private val serverEngine = new ServerEngine(codec = JsonCodec, operationInterceptor = interceptor, operationDecoder = new TextAppOperationDecoder)
  private val defaultChannel = ChannelId.randomId

  def handle(exchange: HttpExchange) {
    try {
      val uri = exchange.getRequestURI
      println("Serving '%s' using %s %s".format(uri, exchange.getProtocol, exchange.getRequestMethod))


      val request = Source.fromInputStream(exchange.getRequestBody).getLines.toList.foldLeft("\n")(_+_)
      val reply = serverEngine.processRequest(request)
      exchange.sendResponseHeaders(200, reply.length)
      exchange.getResponseBody.write(reply.getBytes)
    } catch {
      case e => 
        println("=== Exception while handling request ===")
        e.printStackTrace
        exchange.sendResponseHeaders(500, 0)
        exchange.getResponseBody.write(page500.toString.getBytes)
    } finally {
      exchange.getResponseBody.close
    }
  }
}
