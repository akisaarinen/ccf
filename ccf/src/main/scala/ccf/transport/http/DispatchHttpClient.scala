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

import org.apache.http.params.HttpConnectionParams

import dispatch.Http._

import java.net.URL
import dispatch.{Http => DispatchHttp}
import org.apache.http.conn.scheme.Scheme

class DispatchHttpClient(timeoutMillis: Int, scheme: Option[Scheme]) extends HttpClient {
  def this(timeoutMillis: Int) = this(timeoutMillis, None)
  private val http = new DispatchHttp
  init
  def post(url: URL, data: String): String = http(url.toString.POST << data >- { x => x })
  private def init {
    HttpConnectionParams.setConnectionTimeout(httpClientParams, timeoutMillis)
    HttpConnectionParams.setSoTimeout(httpClientParams, timeoutMillis)
  }
  private def httpClientParams = http.client.getParams
  private[http] def getConnectionManager = http.client.getConnectionManager
}
