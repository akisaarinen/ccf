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

package ccf.session

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.transport.TransportRequest

class SessionRequestFactorySpec extends Specification with Mockito {
  val factory = new SessionRequestFactory
  val emptyTransportRequest = TransportRequest(Map(), None)
  val transportRequestWithUnknownType = TransportRequest(Map("type" -> "wrong type"), None)

  "Session request factory" should {
    "not create request without type" in {
      factory.create(emptyTransportRequest) must throwAn[RuntimeException]
    }

    "not create request with unknown type" in {
      factory.create(transportRequestWithUnknownType) must throwAn[RuntimeException]
    }
  }
}