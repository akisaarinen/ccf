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

package ccf.session

import ccf.transport.{TransportRequestType, TransportRequest}

private object SessionRequestFactory {
  def transportRequest(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): TransportRequest = TransportRequest(
    Map("sequenceId" -> s.seqId.toString, "version" -> s.version.toString, "clientId" -> s.clientId.id.toString,
        "channelId" -> channelId.toString, "type" -> requestType),
    content
  )

  def sessionRequest(transportRequest: TransportRequest): SessionRequest = {
    transportRequest.header("type") match {
      case Some(TransportRequestType.join) => JoinRequest(transportRequest)
      case Some(TransportRequestType.part) => PartRequest(transportRequest)
      case Some(TransportRequestType.context) => OperationContextRequest(transportRequest)
      case Some(requestType) => InChannelRequest(transportRequest)
      case None => error("No request type given")
    }
  }
}