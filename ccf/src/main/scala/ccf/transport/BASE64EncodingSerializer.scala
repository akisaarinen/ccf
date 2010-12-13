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

import sun.misc.{BASE64Encoder, BASE64Decoder}
import java.io._
import ccf.server.StateSerializer

object BASE64EncodingSerializer extends StateSerializer {
  def serialize(op: AnyRef) = {
    encode(JavaSerializer.serialize(op))
  }

  def deserialize[A](in: String)(implicit mf: scala.reflect.Manifest[A]): A = {
    JavaSerializer.deserialize[A](decode(in))
  }

  def encode(serialized: Array[Byte]): String = new BASE64Encoder().encode(serialized)

  def decode(encoded: String): Array[Byte] = new BASE64Decoder().decodeBuffer((encoded))
}

object JavaSerializer {
  def serialize(op: AnyRef): Array[Byte] = {
    val byteStream = new ByteArrayOutputStream()
    val outputStream = new ObjectOutputStream(new BufferedOutputStream(byteStream))
    outputStream.writeObject(op)
    outputStream.close
    byteStream.toByteArray
  }

  def deserialize[A](in: Array[Byte]): A = {
    val inputStream = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(in)))
    inputStream.readObject.asInstanceOf[A]
  }
}