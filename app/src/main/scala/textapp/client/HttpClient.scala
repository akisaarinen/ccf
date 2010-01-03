package textapp.client

import ccf.messaging.ConcurrentOperationMessage
import ccf.transport.ClientId
import ccf.tree.operation.TreeOperation

import dispatch.{:/, Http, Logger, Request}
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonParser.parse
import OperationCoding.{encode, decode}

class HttpClient(clientId: ClientId) {
  val http = new Http {
    override lazy val log = new Logger {
      def info(msg: String, items: Any*) {}
    }
  }
  val host = :/("localhost", 8000)

  private def idStr = clientId.id.toString

  private def base = host / "textapp"
  private def joinUri = base / "join"
  private def quitUri = base / "quit"
  private def addUri = base / "op" / "add"
  private def getUri = base / "op" / "get"

  def join: String = {
    fetch(joinUri, Map()) \ "document" match {
      case JField(_, JString(v)) => v
      case x => error("No document available in join message (%s)".format(x))
    }
  }
  def quit {
    fetch(quitUri, Map())
  }
  def add(op: String) {
    fetch(addUri, Map("op" -> op))
  }
  def get: (String, List[TreeOperation]) = {
    val reply = fetch(getUri, Map())
    val ops = reply \ "ops" match {
      case JField(_, JArray(ops)) => ops.map { op => op match {
        case JString(v) => decode(v) 
        case _ => error("Unknown value type in op list")
      }}
      case _ => error("Unknown ops list in json")
    }
    val hash = reply \ "hash" match {
      case JField(_, JString(v)) => v
      case _ => error("no hash given")
    }
    (hash, ops)
  }

  private def fetch(req: Request, params: Map[String, Any]): JsonAST.JValue = {
    val postReq = req << (Map("id" -> idStr) ++ params)
    http(postReq >- { s => parse(s) })
  }
}

