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

package ccf.transport.http

import org.specs.Specification
import org.specs.mock.Mockito

import java.io.IOException
import java.net.URL

import ccf.transport.json.{JsonEncoder, JsonDecoder}
import ccf.transport._

object HttpConnectionSpec extends Specification with Mockito {
  "Invalid request" should {
    val url = new URL("http://www.url")
    "cause an InvalidRequestException" in {
      val client = mock[HttpClient]
      val decoder = mock[Decoder]
      val encoder = mock[Encoder]
      val conn = new HttpConnection(url, client, decoder, encoder, None, None) 
      conn.send(new TransportRequest(Map[String, String](), None)) must throwA[InvalidRequestException]
    }
  }
  "IOException thrown from HttpClient#post" should {
    val requestData = "data"
    val url = new URL("http://www.url")
    "cause a ConnectionException" in {
      val request = basicRequest
      val decoder = mock[Decoder]
      val encoder = mock[Encoder]
      encoder.encodeRequest(request) returns requestData
      val client = mock[HttpClient]
      client.post(any[URL], any[String]) throws new IOException
      val conn = new HttpConnection(url, client, decoder, encoder, None, None)
      conn.send(request) must throwA[ConnectionException]
    }
  }
  "HttpConnection with header contributor" should {
    val url = new URL("http://www.url")
    "add contributed headers to request" in {
      val originalHeaders = Map("type" -> "sometype")
      val originalContent = Some("content")
      val request = TransportRequest(originalHeaders, originalContent)

      val contributedHeaders = Map("myHeader" -> "myValue")
      val expectedRequest = TransportRequest(originalHeaders ++ contributedHeaders, originalContent)
      val encodedExpectedRequest = JsonEncoder.encodeRequest(expectedRequest)

      val contributor = new HttpTransportHeaderContributor {
        def getHeaders = contributedHeaders
      }

      val response = JsonEncoder.encodeResponse(TransportResponse(Map(), None))
      val client = mock[HttpClient]
      client.post(any[URL], any[String]) returns response
      val conn = new HttpConnection(url, client, JsonDecoder, JsonEncoder, None, Some(contributor))
      conn.send(request)
      there was one(client).post(any[URL], org.mockito.Matchers.eq(encodedExpectedRequest))
    }
  }

  private def basicRequest: TransportRequest = {
    TransportRequest(Map("type" -> "spec"), None)
  }
}
