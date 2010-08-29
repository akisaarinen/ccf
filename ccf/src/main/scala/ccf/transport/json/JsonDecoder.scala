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

package ccf.transport.json

import ccf.transport.Decoder
import ccf.transport.MalformedDataException
import ccf.transport.{Request, Response}
import com.twitter.json.{Json, JsonException}

object JsonDecoder extends Decoder {
  def decodeResponse(m: String) = decodeMessage[Response](m, toResponse)
  def decodeRequest(m: String) = decodeMessage[Request](m, toRequest)
  private def decodeMessage[T](m: String, f: (Map[String, String], Option[Any]) => T) = {
    try { if (m.isEmpty) None else { Some(parse(m, f)) } }
    catch { case e: JsonException => malformedDataException(e.toString) }
  }
  private def parse[T](msg: String, f: (Map[String, String], Option[Any]) => T) = Json.parse(msg) match {
    case m: Map[Any, Any] => f(headers(m), content(m))
    case _                => malformedDataException("Invalid message frame")
  }
  private def headers(m: Map[Any, Any]): Map[String, String] = m.get("headers") match {
    case Some(headers) => headersToMap(headers)
    case None          => malformedDataException("Missing message header")
  }
  private def headersToMap(headers: Any): Map[String, String] = headers match {
    case m: Map[Any, Any] => {
      val seqOfHeaders = for ((k, v) <- m) yield (k.toString, v.toString)
      Map[String, String](seqOfHeaders.toList: _*)
    }
    case _                => malformedDataException("Invalid message header")
  }
  private def content(m: Map[Any, Any]): Option[Any] = m.get("content")
  private def toResponse(h: Map[String, String], c: Option[Any]) = Response(h, c)
  private def toRequest(h: Map[String, String], c: Option[Any]) = Request(h, c)
  private def malformedDataException(s: String) = throw new MalformedDataException(s)
}
