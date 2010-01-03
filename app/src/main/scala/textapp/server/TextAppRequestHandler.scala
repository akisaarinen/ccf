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

  val clients = Map[ClientId, Client]()

  def handle(exchange: HttpExchange) {
    try {
      val uri = exchange.getRequestURI
      //println("Serving " + uri)
      findResource(uri) match {
        case Some(resource) => {
          exchange.sendResponseHeaders(200, resource.length)
          exchange.getResponseBody.write(resource.getBytes)
        }
        case None => exchange.sendResponseHeaders(404, 0)
      }
    } catch {
      case e => 
        println("EXCEPTION WHILE HANDLING")
        println(e)
        exchange.sendResponseHeaders(500, 0)
    } finally {
      exchange.getResponseBody.close
    }
  }

  def findResource(uri: URI): Option[String] = {
    val JoinExpr = new Regex("""/textapp/join/(.*)""")
    val QuitExpr = new Regex("""/textapp/quit/(.*)""")
    val AddExpr = new Regex("""/textapp/op/add/(.*)/(.*)""")
    val GetExpr = new Regex("""/textapp/op/get/(.*)""")

    val json: JsonAST.JValue = uri.toString match {
      case JoinExpr(idStr) =>
        val id = ClientId(java.util.UUID.fromString(idStr))
        clients += (id -> new Client)
        ("status" -> "ok") ~
        ("document" -> document.text)
      case QuitExpr(idStr) =>
        val id = ClientId(java.util.UUID.fromString(idStr))
        clients -= id
        ("status" -> "ok")
      case AddExpr(idStr, encodedOp) => 
        val id = ClientId(java.util.UUID.fromString(idStr))
        val op = decode(encodedOp)
        handleOpFromClient(id, op)
        ("status" -> "ok")
      case GetExpr(idStr) => 
        val id = ClientId(java.util.UUID.fromString(idStr))
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
