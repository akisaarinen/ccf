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

import ccf.transport.{TransportRequestType, TransportResponse}

object SessionResponseFactory {
  val StatusKey = "status"
  val SuccessStatus = "OK"
  val FailureStatus = "FAIL"
  val ResultKey = "result"
  val ReasonKey = "reason"

  def successContent(result: Option[String]) = {
    val content = Map(StatusKey -> SuccessStatus)
    result match {
      case Some(str) => Some(content + (ResultKey -> str))
      case None => Some(content)
    }
  }

  def failureContent(reason: String) =
    Some(Map(StatusKey -> FailureStatus, ReasonKey -> reason))

  private[session] def sessionResponse(transportResponse: TransportResponse, request: SessionRequest): SessionResponse = {
    transportResponse.header("type") match {
      case Some(TransportRequestType.join) => JoinResponse(transportResponse, result(transportResponse, request))
      case Some(TransportRequestType.part) => PartResponse(transportResponse, result(transportResponse, request))
      case Some(TransportRequestType.context) => OperationContextResponse(transportResponse, result(transportResponse, request))
      case Some(customRequestType) => InChannelResponse(transportResponse, result(transportResponse, request))
      case None => error("No response type found")
    }
  }

  private def result(transportResponse: TransportResponse, request: SessionRequest): Either[Success, Failure] = {
    def isNonEmptyStringToStringMap(map: Map[_,_]): Boolean = {
      map.head match {
        case (key: String, value: String) => true
        case _ => false
      }
    }

    transportResponse.content match {
      case Some(content) => content match {
        case contentMap: Map[_,_] if contentMap.isEmpty => Right(Failure(request, "Empty response content"))
        case contentMap: Map[_,_] if !isNonEmptyStringToStringMap(contentMap) => Right(Failure(request, "Mistyped response content"))
        case contentMap: Map[_,_] => {
          val contents = contentMap.asInstanceOf[Map[String,String]]
          contents.get(StatusKey) match {
            case Some(SuccessStatus) => Left(Success(request, contents.get(ResultKey)))
            case Some(FailureStatus) => Right(Failure(request, contents.get(ReasonKey).getOrElse("")))
            case _ => Right(Failure(request, "Unkown response status"))
          }
        }
        case _ => Right(Failure(request, "Unrecognized response content"))
      }
    case _ => Right(Failure(request, "Response missing content"))
    }
  }
}