package textapp.client

import ccf.messaging.ConcurrentOperationMessage
import ccf.transport.ClientId
import ccf.tree.operation.TreeOperation

import dispatch.{:/, Http, Logger, Request}
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonParser.parse
import MessageCoding.{decode}

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
  private def addUri = base / "msg" / "add"
  private def getUri = base / "msg" / "get"
    

  def join: String = {
    fetch(joinUri, Map()) \ "document" match {
      case JField(_, JString(v)) => v
      case x => error("No document available in join message (%s)".format(x))
    }
  }
  def quit {
    fetch(quitUri, Map())
  }
  def add(msg: String) {
    fetch(addUri, Map("msg" -> msg))
  }
  def get: (String, List[ConcurrentOperationMessage[TreeOperation]]) = {
    val replyJson = fetch(getUri, Map())
    val encodedMsgs = replyJson \ "msgs" match {
      case JField(_, JArray(msgs)) => msgs.map(_.toString)
      case _ => error("Unknown msgs list in json")
    }
    val msgs = encodedMsgs.map(decode(_))
    val hash = replyJson \ "hash" match {
      case JField(_, JString(v)) => v
      case _ => error("no hash given")
    }
    (hash, msgs)
  }

  private def fetch(req: Request, params: Map[String, Any]): JsonAST.JValue = {
    val postReq = req << (Map("id" -> idStr) ++ params)
    http(postReq >- { s => parse(s) })
  }
}

