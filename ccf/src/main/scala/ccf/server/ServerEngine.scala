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
import ccf.{JupiterOperationSynchronizerFactory, OperationSynchronizerFactory}
import ccf.tree.JupiterTreeTransformation
import ccf.messaging.OperationContext
import java.io.{StringWriter, PrintWriter, Serializable}

class DefaultServerOperationInterceptor extends ServerOperationInterceptor {
  def currentStateFor(channelId: ChannelId): Serializable = ""
  def applyOperation(server: Server, clientId: ClientId, channelId: ChannelId, op: TreeOperation) {}
  def operationsForCreatingClient(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
  def operationsForAllClients(clientId: ClientId, channelId: ChannelId, op: TreeOperation): List[TreeOperation] = List()
}

class ServerEngine(codec: Codec,
                   operationInterceptor: ServerOperationInterceptor = new DefaultServerOperationInterceptor,
                   transportInterceptor: TransportRequestInterceptor = new DefaultTransportRequestInterceptor,
                   operationSynchronizerFactory: OperationSynchronizerFactory = new JupiterOperationSynchronizerFactory(true, JupiterTreeTransformation),
                   notifyingInterceptor: Option[NotifyingInterceptor] = None) {
  val encodingMimeType = codec.mimeType
  val stateHandler = new StateHandler(operationSynchronizerFactory)
  
  def processRequest(request: String): String = {
    val transportRequest = codec.decodeRequest(request).getOrElse(error("Unable to decode request"))
    val transportResponse = processRequest(transportRequest)
    codec.encodeResponse(transportResponse)
  }

  private def processRequest(transportRequest: TransportRequest): TransportResponse = {
    val sessionRequest = createSessionRequest(transportRequest)
    val (permitted, reason) = transportInterceptor.isPermitted(transportRequest)
    if (permitted) {
      val sessionResponse = processRequest(sessionRequest)
      sessionResponse.transportResponse
    } else {
      sessionRequest.failureResponse(reason).transportResponse
    }
  }

  private def processRequest(sessionRequest: SessionRequest): SessionResponse = {
    sessionRequest match {
      case joinRequest: JoinRequest => onJoin(joinRequest)
      case partRequest: PartRequest => onPart(partRequest)
      case operationRequest: OperationContextRequest => onOperation(operationRequest)
      case inChannelRequest: InChannelRequest => onGetMsgs(inChannelRequest)
      case _ => sessionRequest.successResponse(None)
    }
  }

  private def onJoin(joinRequest: JoinRequest): SessionResponse = {
    val (clientId, channelId) = (joinRequest.clientId, joinRequest.channelId)
    stateHandler.join(clientId, channelId)
    val currentState = operationInterceptor.currentStateFor(channelId).asInstanceOf[AnyRef]
    val serializedState = BASE64EncodingSerializer.serialize(currentState)
    joinRequest.successResponse(Some(serializedState))
  }

  private def onPart(partRequest: PartRequest): SessionResponse = {
    val (clientId, channelId) = (partRequest.clientId, partRequest.channelId)
    stateHandler.part(clientId, channelId)
    partRequest.successResponse(None)
  }

  private def onOperation(operationRequest: OperationContextRequest) = {
    val (clientId: ClientId, channelId: ChannelId) = (operationRequest.clientId, operationRequest.channelId)
    try {
      val content = operationRequest.transportRequest.content.getOrElse(throw new RuntimeException("OperationContextRequest missing"))
      val operationContext = OperationContext(content.asInstanceOf[Map[String, String]])
      val state = stateHandler.clientState(clientId)
      val op = state.receive(operationContext)
      //TODO: server is null in this patch...it is used to set server in serverOperationPersistor which uses it for channel shutdown only
      operationInterceptor.applyOperation(null, channelId, op)

      val others = stateHandler.otherClientsFor(clientId)
      others.foreach { otherClientId =>
        val msgForOther = stateHandler.clientState(otherClientId).send(op)
        stateHandler.addMsg(otherClientId, channelId, msgForOther)
      }

      val opsForCreator = operationInterceptor.operationsForCreatingClient(clientId, channelId, op)
      opsForCreator.foreach { opForCreator =>
        val msgForCreator = stateHandler.clientState(clientId).send(opForCreator)
        stateHandler.addMsg(clientId, channelId, msgForCreator)
      }

      val opsForAll = operationInterceptor.operationsForAllClients(channelId, op)
      opsForAll.foreach { opForAll =>
        //TODO: server is null in this patch...it is used to set server in serverOperationPersistor which uses it for channel shutdown only
        operationInterceptor.applyOperation(null, channelId, opForAll)
        stateHandler.clientsForChannel(channelId).foreach { clientInChannel =>
          val msgForClient = stateHandler.clientState(clientInChannel).send(opForAll)
          stateHandler.addMsg(clientInChannel, channelId, msgForClient)
        }
      }
      notifyingInterceptor.foreach(_.notify(clientId, channelId))
      operationRequest.successResponse(None)
    } catch {
      case e =>
        println("operation request handling error", e)
        operationRequest.failureResponse("operation request handling error: " + stackTraceToString(e))
    }
  }

  private def onGetMsgs(inChannelRequest: InChannelRequest): SessionResponse = {
    val lastMsg = inChannelRequest.content.getOrElse(0).asInstanceOf[Int]
    val (clientId, channelId) = (inChannelRequest.clientId, inChannelRequest.channelId)
    val encodedMsgs = stateHandler.getMsgs(clientId, channelId, lastMsg).map(_.encode)
    inChannelRequest.successResponse(Some(BASE64EncodingSerializer.serialize(encodedMsgs)))
  }

  protected def createSessionRequest(transportRequest: TransportRequest) = SessionRequest(transportRequest)

  private def stackTraceToString(e: Throwable): String = {
    val result = new StringWriter
    val writer = new PrintWriter(result)
    e.printStackTrace(writer)
    result.toString
  }
}