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

import scala.io.Source
import java.io.InputStream
import java.net.URLDecoder

class FormDecoder(inputStream: InputStream) {
  private val source = Source.fromInputStream(inputStream)
  private val sourceAsString = source.getLines.toList.foldLeft("")(_+_)
  private val paramArray = sourceAsString.split("&").filter(_ != "").map(_.split("=")).map { kvPair =>
    if (kvPair.size != 2) throw new RuntimeException("Invalid key-value pair in '%s'".format(kvPair.mkString("=")))
    val (encodedKey, encodedValue) = (kvPair(0), kvPair(1))
    val key = URLDecoder.decode(encodedKey, "UTF-8")
    val value = URLDecoder.decode(encodedValue, "UTF-8")
    (key, value)
  }
  val params = Map(paramArray: _*)
}

