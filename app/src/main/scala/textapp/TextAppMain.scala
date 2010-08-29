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

package textapp

import textapp.server.ServerApp
import textapp.client.ClientApp

object TextAppMain {
  private val defaultHost = "localhost"
  private val defaultPort = "9999"

  def main(args: Array[String]) {
    val clap = new CommandLineArgumentProcessor(args)
    val host = clap.getParam("host", "h").getOrElse(defaultHost)
    val port = clap.getParam("port", "p").getOrElse(defaultPort)

    clap.getParam("server", "s") match {
      case Some(_) => runServer(port.toInt)
      case None => runClient(host, port.toInt)
    }
  }

  private def runServer(port: Int) {
    new ServerApp(port)
    println("Server application started at port %d".format(port))
  }

  private def runClient(host: String, port: Int) {
    val clientApp = new ClientApp(host, port)
    println("Client application started'")
  }
}

