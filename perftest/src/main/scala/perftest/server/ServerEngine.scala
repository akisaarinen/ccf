package perftest.server

import ccf.transport.Request
import ccf.OperationContext
import ccf.tree.operation.TreeOperationDecoder
import ccf.session.{OperationContextRequest, PartRequest, JoinRequest, AbstractRequest}

class ServerEngine {
  def decodeRequest(request: Option[Request]) {
    request match {
      case Some(r: Request) => {
        try {
          handleRequest(AbstractRequest.sessionRequest(r))
        } catch {
          case ex: Exception => fatalError(ex.getMessage)
        }
      }
      case None => fatalError("Unable to decode request")
    }
  }

  private def handleRequest(sessionRequest: AbstractRequest) {
    sessionRequest match {
      case r: JoinRequest =>
      case r: PartRequest =>
      case r: OperationContextRequest => {
        val encodedContext = r.transportRequest.content.get.asInstanceOf[Map[String, Any]]
        val op = newOperationDecoder.decode(encodedContext("op"))
        val localMsgSeqNo = encodedContext("localMsgSeqNo").asInstanceOf[Int]
        val remoteMsgSeqNo = encodedContext("remoteMsgSeqNo").asInstanceOf[Int]
        val context = new OperationContext(op, localMsgSeqNo, remoteMsgSeqNo)
      }
    }
  }

  protected def fatalError(msg: String) {
    error("Unable to decode request")
  }

  protected def newOperationDecoder = new TreeOperationDecoder {
    protected def parseModifier(encodedValue: Any) = null
    protected def parseNode(encodedValue: Any) = null
  }
}
