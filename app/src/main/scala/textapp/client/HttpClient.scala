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

package textapp.client

import ccf.transport.ClientId
import ccf.tree.operation.TreeOperation

import dispatch.{:/, Http, Logger, Request}
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonParser.parse
import textapp.messaging.MessageCoder
import ccf.messaging.Message

class HttpClient(hostname: String, port: Int, clientId: ClientId) {
  private val http = new Http {
    override lazy val log = new Logger {
      def info(msg: String, items: Any*) {}
    }
  }
  private val host = :/(hostname, port)
  private val messageCoder = new MessageCoder

  private def idStr = clientId.id.toString

  private def base = host / "textapp"
  private def joinUri = base / "join"
  private def quitUri = base / "quit"
  private def addUri = base / "msg" / "add"
  private def getUri = base / "msg" / "get"
    

  def join: String = {
    fetch(joinUri, Map()) \ "document" match {
      case JField(_, JString(v)) => v
      case x => error("No document available in join message (%s)".format(x))
    }
  }
  def quit {
    fetch(quitUri, Map())
  }
  def add(msg: String) {
    fetch(addUri, Map("msg" -> msg))
  }
  def get: (String, List[Message]) = {
    val replyJson = fetch(getUri, Map())
    val encodedMsgs = replyJson \ "msgs" match {
      case JField(_, JArray(msgs)) => msgs.map(_.values.toString)
      case _ => error("Unknown msgs list in json")
    }
    val msgs = encodedMsgs.map(messageCoder.decode(_))
    val hash = replyJson \ "hash" match {
      case JField(_, JString(v)) => v
      case _ => error("no hash given")
    }
    (hash, msgs)
  }

  private def fetch(req: Request, params: Map[String, Any]): JsonAST.JValue = {
    val postReq = req << (Map("id" -> idStr) ++ params)
    http(postReq >- { s => parse(s) })
  }
}

