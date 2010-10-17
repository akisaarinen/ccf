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

package ccf.server

import ccf.tree.operation.TreeOperation
import ccf.session._
import ccf.transport.{BASE64EncodingSerializer, Codec, TransportResponse, TransportRequest}

class DefaultServerOperationInterceptor extends ServerOperationInterceptor {
  def currentStateFor(channelId: ChannelId) = ""
  def applyOperation(server: Server, clientId: ClientId, channelId: ChannelId, op: TreeOperation) {}
  def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
  def operationsForAllClients(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
}

class ServerEngine(codec: Codec,
                   operationInterceptor: ServerOperationInterceptor = new DefaultServerOperationInterceptor,
                   transportInterceptor: TransportRequestInterceptor = new DefaultTransportRequestInterceptor) {
  val encodingMimeType = codec.mimeType

  def processRequest(request: String): String = {
    val transportRequest = codec.decodeRequest(request).getOrElse(error("Unable to decode request"))
    val transportResponse = processRequest(transportRequest)
    codec.encodeResponse(transportResponse)
  }

  protected def sessionRequest(transportRequest: TransportRequest) = SessionRequest(transportRequest)

  private def processRequest(transportRequest: TransportRequest): TransportResponse = {
    processRequest(sessionRequest(transportRequest)).transportResponse
  }

  private def processRequest(sessionRequest: SessionRequest): SessionResponse = {
    sessionRequest match {
      case joinRequest: JoinRequest => onJoin(joinRequest)
      case _ => sessionRequest.successResponse(None)
    }
  }

  private def onJoin(joinRequest: JoinRequest): SessionResponse = {
    val (clientId, channelId) = (joinRequest.clientId, joinRequest.channelId)
    joinRequest.successResponse(Some(BASE64EncodingSerializer.serialize(operationInterceptor.currentStateFor(channelId).asInstanceOf[AnyRef])))
  }
}
