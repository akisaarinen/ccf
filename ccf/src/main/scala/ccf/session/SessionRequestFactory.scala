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

import ccf.transport.TransportRequest

object SessionRequestFactory {
  val SequenceIdKey = "sequenceId"
  val VersionKey = "version"
  val ClientIdKey = "clientId"
  val ChannelIdKey = "channelId"
  val TypeKey = "type"
  val JoinType = "channel/join"
  val PartType = "channel/part"
  val SessionControlTypes = Set(JoinType, PartType)
  val OperationContextType = "channel/context"

  def transportRequestHeaders(s: Session, requestType: String, channelId: ChannelId): Map[String, String] =
    Map(SequenceIdKey -> s.seqId.toString, VersionKey -> s.version.toString, ClientIdKey -> s.clientId.id.toString,
        ChannelIdKey -> channelId.toString, TypeKey -> requestType)

  def transportRequest(s: Session, requestType: String, channelId: ChannelId, content: Option[Any]): TransportRequest =
    TransportRequest(transportRequestHeaders(s, requestType, channelId), content)

  private[session] def sessionRequest(transportRequest: TransportRequest): SessionRequest = {
    transportRequest.header(TypeKey) match {
      case Some(JoinType) => JoinRequest(transportRequest, channelIdFromContent(transportRequest))
      case Some(PartType) => PartRequest(transportRequest)
      case Some(OperationContextType) => OperationContextRequest(transportRequest)
      case Some(requestType) => InChannelRequest(transportRequest)
      case None => error("No request type given")
    }
  }

  private def channelIdFromMap(map: Map[String, String]): ChannelId = {
    val channelIdValue = map.get(ChannelIdKey).getOrElse(error(ChannelIdKey + " missing"))
    ChannelId(channelIdValue).getOrElse(error("Invalid value for " + ChannelIdKey))
  }

  private def channelIdFromContent(transportRequest: TransportRequest): ChannelId = {
    def isNonEmptyStringToStringMap(map: Map[_, _]) = {
      map.head match {
        case (_: String, _: String) => true
        case _ => false
      }
    }

    transportRequest.content match {
      case Some(m: Map[_, _]) if (m.isEmpty) => error("Empty request content")
      case Some(m: Map[_, _]) if (isNonEmptyStringToStringMap(m)) => channelIdFromMap(m.asInstanceOf[Map[String, String]])
      case _ => error("Unknown request content type")
    }
  }
}
