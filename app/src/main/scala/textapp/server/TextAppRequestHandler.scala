package textapp.server

import MessageCoding.{encode, decode}
import ccf.{JupiterOperationSynchronizer, JupiterOperationSynchronizerFactory}
import ccf.messaging.ConcurrentOperationMessage
import ccf.transport.{ClientId, ChannelId}
import ccf.tree.JupiterTreeTransformation
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import ccf.transport.{Event, TransportActor}
import ccf.server.Server
import com.sun.net.httpserver.{HttpHandler, HttpExchange}
import java.net.URI
import java.util.UUID
import scala.collection.mutable.{ArrayBuffer, Map}
import scala.util.matching.Regex

class TextAppRequestHandler extends HttpHandler {
  import net.liftweb.json.JsonAST
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  private val document = new TextDocument("")
  
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

  private var messages = List[Event.Msg[TreeOperation]]()
  private val defaultChannel = ChannelId.randomId
    
  private val factory = new JupiterOperationSynchronizerFactory(true, JupiterTreeTransformation)
  private val interceptor = new TextAppOperationInterceptor(document)
  private val transport = new TextAppTransportActor(onMessageToClient)
  private val server = new Server[TreeOperation](factory, interceptor, transport)

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

  private def findResource(uri: URI, params: scala.collection.immutable.Map[String, String]): Option[String] = {
    val JoinExpr = new Regex("""/textapp/join""")
    val QuitExpr = new Regex("""/textapp/quit""")
    val AddExpr = new Regex("""/textapp/msg/add""")
    val GetExpr = new Regex("""/textapp/msg/get""")
        
    val id = ClientId(java.util.UUID.fromString(params("id")))

    val json: JsonAST.JValue = uri.toString match {
      case JoinExpr() =>
        server !? Event.Join(id, defaultChannel)
        ("status" -> "ok") ~
        ("document" -> document.text)
      case QuitExpr() =>
        server !? Event.Quit(id, defaultChannel)
        ("status" -> "ok")
      case AddExpr() => 
        val encodedMsg = params("msg")
        val msg = decode(encodedMsg)
        server !? Event.Msg(id, defaultChannel, msg)
        ("status" -> "ok")
      case GetExpr() => 
        val msgs = getMsgsForClient(id).map(encode(_))
        ("status" -> "ok") ~
        ("msgs" -> msgs) ~
        ("hash" -> document.hash)
      case _ => 
        ("status" -> "error") ~
        ("error" -> "unknown uri")
    }
    Some(compact(JsonAST.render(json)))
  }

  private def onMessageToClient(msg: Event.Msg[TreeOperation]): Unit = messages.synchronized {
    messages = messages ::: List(msg)
  }

  private def getMsgsForClient(id: ClientId): List[ConcurrentOperationMessage[TreeOperation]] = messages.synchronized {
    def isForClient(msg: Event.Msg[_]) = msg match {
      case Event.Msg(clientId, _, _) if (id == clientId) => true
      case _ => false
    }

    val msgsForClient = messages.filter(isForClient(_))
    val msgsNotForClient = messages.filter(!isForClient(_))
    messages = msgsNotForClient
    msgsForClient.map(_.msg.asInstanceOf[ConcurrentOperationMessage[TreeOperation]])
  }
}
