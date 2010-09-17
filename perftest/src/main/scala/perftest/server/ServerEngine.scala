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

package perftest.server

import ccf.transport.TransportRequest
import ccf.OperationContext
import ccf.tree.operation.TreeOperationDecoder
import ccf.session.{OperationContextRequest, PartRequest, JoinRequest, SessionRequest}

class ServerEngine {
  def decodeRequest(request: Option[TransportRequest]) {
    request match {
      case Some(r: TransportRequest) => {
        try {
          handleRequest(SessionRequest.sessionRequest(r))
        } catch {
          case ex: Exception => fatalError(ex.getMessage)
        }
      }
      case None => fatalError("Unable to decode request")
    }
  }

  private def handleRequest(sessionRequest: SessionRequest) {
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
