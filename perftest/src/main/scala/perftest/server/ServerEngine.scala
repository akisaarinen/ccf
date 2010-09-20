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

import ccf.OperationContext
import ccf.tree.operation.TreeOperationDecoder
import ccf.session._
import ccf.transport.{TransportResponse, TransportRequest}

class ServerEngine {
  private val sessionRequestFactory = new SessionRequestFactory

  def decodeRequest(request: Option[TransportRequest]) : TransportResponse = {
    request match {
      case Some(r: TransportRequest) => {
        try {
          val sessionRequest = sessionRequestFactory.create(r)
          val sessionReply = handleRequest(sessionRequest)
          sessionReply.transportResponse
        } catch {
          case ex: Exception => error(ex.getMessage)
        }
      }
      case None => error("Unable to decode request")
    }
  }

  private def handleRequest(sessionRequest: SessionRequest) : SessionResponse = {
    sessionRequest match {
      case JoinRequest(_) => sessionRequest.successResponse(None)
      case PartRequest(_) => sessionRequest.successResponse(None)
      case InChannelRequest(_) => sessionRequest.successResponse(None)
      case OperationContextRequest(tr) => {
        val encodedContext = tr.content.get.asInstanceOf[Map[String, Any]]
        val op = newOperationDecoder.decode(encodedContext("op"))
        val localMsgSeqNo = encodedContext("localMsgSeqNo").asInstanceOf[Int]
        val remoteMsgSeqNo = encodedContext("remoteMsgSeqNo").asInstanceOf[Int]
        val context = new OperationContext(op, localMsgSeqNo, remoteMsgSeqNo)
        sessionRequest.successResponse(None)
      }
    }
  }

  protected def newOperationDecoder = new TreeOperationDecoder {
    protected def parseModifier(encodedValue: Any) = null
    protected def parseNode(encodedValue: Any) = null
  }
}
