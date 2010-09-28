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

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.transport.json.{JsonDecoder, JsonEncoder}
import java.util.UUID
import ccf.OperationContext
import ccf.tree.operation.NoOperation

class RequestResponseIntegrationSpec extends Specification with Mockito {
  val session = mock[Session]
  session.seqId returns 1
  session.version returns Version(2, 3)
  session.clientId returns ClientId(new UUID(4, 5))
  val channelId = new ChannelId(new UUID(6, 7))

  "Round-trip encode-decode join request, generate and encode-decode response" should {
    val request = JoinRequest(session, channelId)

    roundTripResponseToRequestSpec(request, _ must haveClass[JoinResponse])
  }

  "Round-trip encode-decode part request, generate and encode-decode response" should {
    val request = PartRequest(session, channelId)

    roundTripResponseToRequestSpec(request, _ must haveClass[PartResponse])
  }

  "Round-trip encode-decode in-channel request, generate and encode-decode response" should {
    val content = Some("test content")
    val requestType = "test/type"
    val request = InChannelRequest(session, requestType, channelId, content)

    roundTripResponseToRequestSpec(request, _ must haveClass[InChannelResponse])
  }

  "Round-trip encode-decode operation context request, generate and encode-decode response" should {
    val context = OperationContext(NoOperation(), 1, 2)
    val request = OperationContextRequest(session, channelId, context)
    
    roundTripResponseToRequestSpec(request, _ must haveClass[OperationContextResponse])
  }

  def roundTripResponseToRequestSpec(request: SessionRequest, verifyResponseType: (SessionResponse) => Unit) {
    "result in correct success response without result" in {
      val genResponse = (request: SessionRequest) => request.successResponse(None)
      val response = roundTripResponseToRequest(request, genResponse)
      verifyResponseType(response)
      response must equalTo(genResponse(request))
    }

    "result in correct success response with result" in {
      val testResult = Some("test result")
      val genResponse = (request: SessionRequest) => request.successResponse(testResult)
      val response = roundTripResponseToRequest(request, genResponse)
      verifyResponseType(response)
      response must equalTo(genResponse(request))
    }

    "result in correct failure response" in {
      val testReason = "test reason"
      val genResponse = (request: SessionRequest) => request.failureResponse(testReason)
      val response = roundTripResponseToRequest(request, genResponse)
      verifyResponseType(response)
      response must equalTo(genResponse(request))
    }
  }

  def encodeAndDecodeRequest(request: SessionRequest): SessionRequest = {
    val transportRequest = request.transportRequest
    val encodedRequest = JsonEncoder.encodeRequest(transportRequest)
    val decodedRequest = JsonDecoder.decodeRequest(encodedRequest).get
    SessionRequest.sessionRequest(decodedRequest)
  }

  def encodeAndDecodeResponse(response: SessionResponse, request: SessionRequest): SessionResponse = {
    val encodedResponse = JsonEncoder.encodeResponse(response.transportResponse)
    val decodedResponse = JsonDecoder.decodeResponse(encodedResponse).get
    SessionResponse(decodedResponse, request)
  }

  def roundTripResponseToRequest(sr: SessionRequest, genResponse: ((SessionRequest) => SessionResponse)): SessionResponse = {
    encodeAndDecodeResponse(genResponse(encodeAndDecodeRequest(sr)), sr)
  }
}