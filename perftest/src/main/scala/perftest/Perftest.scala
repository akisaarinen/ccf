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

package perftest

import perftest.client.Client
import perftest.server.Server

import java.net.URL

object Perftest {
  private def usage = println("Usage: Perftest [client|server] URL")
  private def parse(args: Array[String]) = if (args.length > 1) Some(args(0), new URL(args(1))) else None
  def main(args: Array[String]) = parse(args) match {
    case Some(("server", url)) => Server.run(url)
    case Some(("client", url)) => Client.run(url)
    case None                  => usage
  }
}
