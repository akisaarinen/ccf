package textapp.client

import ccf.messaging.ConcurrentOperationMessage
import ccf.transport.ClientId
import ccf.tree.operation.TreeOperation


class HttpClient(id: ClientId) {
  import dispatch._
  import Http._
  import dispatch.json.JsHttp._

  import net.liftweb.json._
  import net.liftweb.json.JsonAST
  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.JsonParser.parse

  import OperationCoding.{encode, decode}

  val http = new Http {
    override lazy val log = new Logger {
      def info(msg: String, items: Any*) {}
    }
  }
  val host = :/("localhost", 8000)

  private def idStr = id.id.toString

  private def base = host / "textapp"
  private def joinUri = base / "join" / idStr
  private def quitUri = base / "quit" / idStr
  private def addUri(op: String) = base / "op" / "add" / idStr / op
  private def getUri = base / "op" / "get" / idStr

  def join: String = {
    fetch(joinUri) \ "document" match {
      case JField(_, JString(v)) => v
      case x => error("No document available in join message (%s)".format(x))
    }
  }
  def quit {
    fetch(quitUri)
  }
  def add(op: String) {
    fetch(addUri(op))
  }
  def get: (String, List[TreeOperation]) = {
    val reply = fetch(getUri)
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

  private def fetch(req: Request): JsonAST.JValue = {
    http(req >- { s => parse(s) })
  }
}

