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

object ClientIdSpec extends Specification {
  "Random ClientId" should {
    val clientId = ClientId.randomId
    "not be equal to another random ClientId" in {
      clientId must not equalTo(ClientId.randomId)
    }
  }

  "ClientId" should {
    "encode and decode transport representation of client id" in {
      val clientId = ClientId.randomId
      val tranportRepresentation = clientId.toString
      ClientId(tranportRepresentation) must equalTo(Some(clientId))
    }
    "return None for invalid representation of client id" in {
      val invalidId = "not an uuid"
      ClientId(invalidId) mustBe None
    }
  }
}
