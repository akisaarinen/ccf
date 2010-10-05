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

package ccf.transport

import javax.activation.MimeType

trait Codec extends Encoder with Decoder {
  val mimeType: MimeType
  val encoder: Encoder
  val decoder: Decoder

  def encodeRequest(request: TransportRequest): String = encoder.encodeRequest(request)
  def encodeResponse(response: TransportResponse): String = encoder.encodeResponse(response)

  @throws(classOf[MalformedDataException])
  def decodeResponse(msg: String): Option[TransportResponse] = decoder.decodeResponse(msg)
  @throws(classOf[MalformedDataException])
  def decodeRequest(msg: String): Option[TransportRequest] = decoder.decodeRequest(msg)
}
