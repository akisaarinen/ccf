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

package textapp.server

import com.sun.net.httpserver.{HttpServer}
import java.net.InetSocketAddress

class ServerApp(port: Int) {
  try {
    val server = HttpServer.create(new InetSocketAddress(port), 0)
    server.createContext("/textapp", new TextAppRequestHandler)
    server.setExecutor(null) // creates a default executor
    server.start()
  } catch {
    case e =>
      println("Error while starting server")
      println(e)
  }
}
