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

import java.io.IOException
import java.net.URL

import ccf.transport.Connection
import ccf.transport.json.{JsonEncoder, JsonDecoder}
import ccf.transport.{ConnectionException, InvalidRequestException}
import ccf.transport.{Encoder, Decoder}
import ccf.transport.{TransportRequest, TransportResponse}
import org.apache.http.conn.scheme.Scheme

object HttpConnection {
  private val timeoutMillis = 1000
  def create(url: URL, scheme: Option[Scheme] = None) = new HttpConnection(url, new DispatchHttpClient(timeoutMillis, scheme), JsonDecoder, JsonEncoder)
}

class HttpConnection(url: URL, client: HttpClient, decoder: Decoder, encoder: Encoder, scheme: Option[Scheme]) extends Connection {
  def this(url: URL, client: HttpClient, decoder: Decoder, encoder: Encoder) = this(url, client, decoder, encoder, None)
  def send(request: TransportRequest): Option[TransportResponse] = try {
    decoder.decodeResponse(post(request))
  } catch {
    case e: IOException => throw new ConnectionException(e.toString)
  }
  private def post(request: TransportRequest) = client.post(requestUrl(request), encoder.encodeRequest(request))
  private def requestUrl(request: TransportRequest) = new URL(url, request.header("type").getOrElse(requestTypeMissing))
  private def requestTypeMissing = throw new InvalidRequestException("Request header \"type\" missing")
}
