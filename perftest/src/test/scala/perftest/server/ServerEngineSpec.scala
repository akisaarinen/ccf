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

import org.specs.Specification
import org.specs.mock.{Mockito, MockitoMatchers}
import ccf.transport.{TransportResponse, TransportRequest}
import ccf.tree.operation.{NoOperation, TreeOperationDecoder}
import ccf.OperationContext
import ccf.session.{SessionRequestFactory, SessionResponseFactory}

class ServerEngineSpec extends Specification with Mockito with MockitoMatchers  {
  "ServerEngine" should {
    val operationDecoderMock = mock[TreeOperationDecoder]

    class TestServerEngine extends ServerEngine {
      override def newOperationDecoder = operationDecoderMock
    }
    
    val engine = spy(new TestServerEngine)

    "decode None as an error" in {
      engine.decodeRequest(None) must throwAn(new RuntimeException("Unable to decode request"))
    }

    "decode request header type" in {
      val request = mock[TransportRequest]

      "None as an error" in {
        request.header("type") returns None
        engine.decodeRequest(Some(request)) must throwAn(new RuntimeException("No request type given"))
      }
    }

    "return correct response for" in {
      val commonTransportHeaders = Map("sequenceId" -> "1", "version" -> "2",
        "clientId" -> "3", "channelId" -> "4")
      val channelIdContent = Some(Map("channelId" -> "5"))

      "join request" in {
        val transportRequest = TransportRequest(commonTransportHeaders + ("type" -> SessionRequestFactory.JoinType), channelIdContent)
        val expectedTransportResponse = TransportResponse(transportRequest.headers, SessionResponseFactory.successContent(None))

        engine.decodeRequest(Some(transportRequest)) must equalTo(expectedTransportResponse)
      }

      "part request" in {
        val transportRequest = TransportRequest(commonTransportHeaders + ("type" -> SessionRequestFactory.PartType), channelIdContent)
        val expectedTransportResponse = TransportResponse(transportRequest.headers, SessionResponseFactory.successContent(None))

        engine.decodeRequest(Some(transportRequest)) must equalTo(expectedTransportResponse)
      }

      "in-channel request" in {
        val requestType = "test/type"
        val transportRequest = TransportRequest(commonTransportHeaders + ("type" -> requestType), channelIdContent)
        val expectedTransportResponse = TransportResponse(transportRequest.headers, SessionResponseFactory.successContent(None))

        engine.decodeRequest(Some(transportRequest)) must equalTo(expectedTransportResponse)
      }

      "context request" in {
        val context = OperationContext(NoOperation(), 1, 2)
        val transportRequestContent = Some(context.encode)
        val transportRequest = TransportRequest(commonTransportHeaders + ("type" -> SessionRequestFactory.OperationContextType), transportRequestContent)
        val expectedTransportResponse = TransportResponse(transportRequest.headers, SessionResponseFactory.successContent(None))

        engine.decodeRequest(Some(transportRequest)) must equalTo(expectedTransportResponse)
      }
    }
  }
}
