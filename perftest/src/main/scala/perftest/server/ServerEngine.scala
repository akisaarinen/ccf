package perftest.server

import ccf.transport.Request
import ccf.OperationContext
import ccf.session.AbstractRequest
import ccf.tree.operation.TreeOperationDecoder

class ServerEngine {
  def decodeRequest(request: Option[Request]) {
    val operationDecoder = newOperationDecoder

    request match {
      case Some(r: Request) => {
        r.header("type") match {
          case Some(AbstractRequest.joinRequestType) =>
          case Some(AbstractRequest.partRequestType) =>
          case Some(AbstractRequest.contextRequestType) => {
            val encodedContext = r.content.get.asInstanceOf[Map[String, Any]]
            val op = operationDecoder.decode(encodedContext("op"))
            val localMsgSeqNo = encodedContext("localMsgSeqNo").asInstanceOf[Int]
            val remoteMsgSeqNo = encodedContext("remoteMsgSeqNo").asInstanceOf[Int]
            val context = new OperationContext(op, localMsgSeqNo, remoteMsgSeqNo)
          }
          case Some(unknownRequestType) => fatalError("Unknown request type: " + unknownRequestType)
          case None => fatalError("No request type given")
        }
      }
      case None => fatalError("Unable to decode request")
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
