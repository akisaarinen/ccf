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

import ccf.transport.Request
import ccf.transport.{ConnectionException, InvalidRequestException}
import ccf.transport.{Encoder, Decoder}

object HttpConnectionSpec extends Specification with Mockito {
  "Invalid request" should {
    val url = new URL("http://www.url")
    "cause an InvalidRequestException" in {
      val client = mock[HttpClient]
      val parser = mock[Decoder]
      val formatter = mock[Encoder]
      val conn = new HttpConnection(url, client, parser, formatter) 
      conn.send(new Request(Map[String, String](), None)) must throwA[InvalidRequestException]
    }
  }
  "IOException thrown from HttpClient#post" should {
    val requestData = "data"
    val spec = "spec"
    val url = new URL("http://www.url")
    "cause a ConnectionException" in {
      val request = mock[Request]
      request.header("type") returns Some(spec)
      val formatter = mock[Encoder]
      formatter.encodeRequest(request) returns requestData
      val client = mock[HttpClient]
      client.post(new URL(url, spec), requestData) throws new IOException
      val conn = new HttpConnection(url, client, mock[Decoder], formatter)
      conn.send(request) must throwA[ConnectionException]
    }
  }
}
