package textapp.server

import OperationCoding.{encode, decode}
import ccf.JupiterOperationSynchronizer
import ccf.messaging.ConcurrentOperationMessage
import ccf.transport.ClientId
import ccf.tree.JupiterTreeTransformation
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import com.sun.net.httpserver.{HttpHandler, HttpExchange}
import java.net.URI
import java.util.UUID
import scala.collection.mutable.{ArrayBuffer, Map}
import scala.util.matching.Regex

class TextAppRequestHandler extends HttpHandler {
  import net.liftweb.json.JsonAST
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._

  val document = new TextDocument("")

  class Client {
    val serverSync = new JupiterOperationSynchronizer[TreeOperation](true, JupiterTreeTransformation)
    val clientSync = new JupiterOperationSynchronizer[TreeOperation](false, JupiterTreeTransformation)
    val msgsToClient = new ArrayBuffer[ConcurrentOperationMessage[TreeOperation]]()
  }
  
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

  val clients = Map[ClientId, Client]()

  def handle(exchange: HttpExchange) {
    try {
      import scala.io.Source
      val uri = exchange.getRequestURI
      println("Serving '%s' using %s %s".format(uri, exchange.getProtocol, exchange.getRequestMethod))

      val body = Source.fromInputStream(exchange.getRequestBody).getLines.toList.foldLeft("")(_+_)

      import java.net.URLDecoder
      val paramArray = body.split("&").map(_.split("=")).map { kvPair => 
        val (encodedKey, encodedValue) = (kvPair(0), kvPair(1))
        val key = URLDecoder.decode(encodedKey)
        val value = URLDecoder.decode(encodedValue)
        (key, value)
      }
      val params = Map(paramArray: _*)
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

  def findResource(uri: URI, params: Map[String, String]): Option[String] = {
    val JoinExpr = new Regex("""/textapp/join""")
    val QuitExpr = new Regex("""/textapp/quit""")
    val AddExpr = new Regex("""/textapp/op/add""")
    val GetExpr = new Regex("""/textapp/op/get""")
        
    val id = ClientId(java.util.UUID.fromString(params("id")))

    val json: JsonAST.JValue = uri.toString match {
      case JoinExpr() =>
        clients += (id -> new Client)
        ("status" -> "ok") ~
        ("document" -> document.text)
      case QuitExpr() =>
        clients -= id
        ("status" -> "ok")
      case AddExpr() => 
        val encodedOp = params("op")
        val op = decode(encodedOp)
        handleOpFromClient(id, op)
        ("status" -> "ok")
      case GetExpr() => 
        val ops = getOpsForClient(id).map(encode(_))
        ("status" -> "ok") ~
        ("ops" -> ops) ~
        ("hash" -> document.hash)
      case _ => 
        ("status" -> "error") ~
        ("error" -> "unknown uri")
    }
        
    Some(compact(JsonAST.render(json)))
  }

  def handleOpFromClient(id: ClientId, op: TreeOperation) {
    val client = clients(id)
    val msg = client.clientSync.createLocalOperation(op)
    val opInServer = client.serverSync.receiveRemoteOperation(msg)
    applyOpInServer(_ != id, opInServer)
    println("text in server: " + document.text)
  }

  def getOpsForClient(id: ClientId): List[TreeOperation] = {
    val client = clients(id)
    val msgs = client.msgsToClient.toList
    client.msgsToClient.clear
    msgs.map(client.clientSync.receiveRemoteOperation(_))
  }

  def applyOpInServer(toClients: ClientId => Boolean, op: TreeOperation) {
    document.applyOp(op)
    clients.filter { case (id, _) => toClients(id) }.foreach { case(id, client) =>
      client.msgsToClient += client.serverSync.createLocalOperation(op)
    }
  }
}
