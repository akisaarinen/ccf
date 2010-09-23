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

import ccf.transport.ChannelId
import com.sun.net.httpserver.{HttpHandler, HttpExchange}
import java.net.URI
import java.util.UUID
import scala.collection.immutable.Map
import scala.util.matching.Regex
import textapp.messaging.MessageCoder
import ccf.session.ClientId

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

  private val documentHandler = new DocumentHandler
  private val defaultChannel = ChannelId.randomId
  private val messageCoder = new MessageCoder

  def handle(exchange: HttpExchange) {
    try {
      val uri = exchange.getRequestURI
      println("Serving '%s' using %s %s".format(uri, exchange.getProtocol, exchange.getRequestMethod))

      val params = new FormDecoder(exchange.getRequestBody).params
      findResource(uri, params) match {
        case Some(resource) => {
          exchange.sendResponseHeaders(200, resource.length)
          exchange.getResponseBody.write(resource.getBytes)
        }
        case None => {
          exchange.sendResponseHeaders(404, 0)
          exchange.getResponseBody.write(page404.toString.getBytes)
        }
      }
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

  private def findResource(uri: URI, params: Map[String, String]): Option[String] = {
    val JoinExpr = new Regex("""/textapp/join""")
    val QuitExpr = new Regex("""/textapp/quit""")
    val AddExpr = new Regex("""/textapp/msg/add""")
    val GetExpr = new Regex("""/textapp/msg/get""")
        
    val id = ClientId(java.util.UUID.fromString(params("id")))
  
    import net.liftweb.json.JsonAST
    import net.liftweb.json.JsonDSL._

    val json: JsonAST.JValue = uri.toString match {
      case JoinExpr() =>
        val document = documentHandler.onJoin(id, defaultChannel)
        ("status" -> "ok") ~
        ("document" -> document.text)
      case QuitExpr() =>
        documentHandler.onQuit(id, defaultChannel)
        ("status" -> "ok")
      case AddExpr() => 
        val encodedMsg = params("msg")
        val msg = messageCoder.decode(encodedMsg)
        documentHandler.onMsg(id, defaultChannel, msg)
        ("status" -> "ok")
      case GetExpr() => 
        val (msgs, document) = documentHandler.getMessages(id)
        val encodedMsgs = msgs.map(messageCoder.encode(_))
        ("status" -> "ok") ~
        ("msgs" -> encodedMsgs) ~
        ("hash" -> document.hash)
      case _ => 
        ("status" -> "error") ~
        ("error" -> "unknown uri")
    }
    Some(compact(JsonAST.render(json)))
  }
}
