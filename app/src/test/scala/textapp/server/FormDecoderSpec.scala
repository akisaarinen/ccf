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

import org.specs.Specification
import org.specs.mock.Mockito
import java.io.{ByteArrayInputStream}

object FormDecoderSpec extends Specification with Mockito {
  "FormDecoder" should {
    "return empty map for empty string" in {
      decode("") must equalTo(Map.empty)
    }
    "decode a single key-value pair" in {
      decode("key=value") must equalTo(Map("key" -> "value"))
    }
    "decode two key-value pairs" in {
      decode("key=value&foo=bar") must equalTo(Map("key" -> "value", "foo" -> "bar"))
    }
    "decode encoded characters" in {
      decode("a%20string=me%26my") must equalTo(Map("a string" -> "me&my"))
    }
    "throw exception if key-value pair contains too many equal-signs" in {
      decode("aaa=bbb=ccc") must throwAn[Exception]
    }
    "throw exception if key-value pair contains no equal-sign" in {
      decode("aaa") must throwAn[Exception]
    }
  }

  private def decode(s: String) = {
    val stream = new ByteArrayInputStream(s.getBytes)
    new FormDecoder(stream).params
  }
}

