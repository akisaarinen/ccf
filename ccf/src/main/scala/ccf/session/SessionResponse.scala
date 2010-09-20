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

import ccf.transport.TransportResponse

sealed abstract class SessionResponse {
  val transportResponse: TransportResponse
  val result: Either[Success, Failure] 
}

case class JoinResponse(transportResponse: TransportResponse, result: Either[Success, Failure]) extends SessionResponse

case class PartResponse(transportResponse: TransportResponse, result: Either[Success, Failure]) extends SessionResponse

case class InChannelResponse(transportResponse: TransportResponse, result: Either[Success, Failure]) extends SessionResponse

case class OperationContextResponse(transportResponse: TransportResponse, result: Either[Success, Failure]) extends SessionResponse
