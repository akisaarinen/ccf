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

package textapp.messaging

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.messaging.OperationContext
import ccf.tree.operation.TreeOperation

object MessageCoderSpec extends Specification with Mockito {
  val operationCoder = mock[OperationCoder]
  val mockedOp = mock[TreeOperation]
  operationCoder.encode(mockedOp) returns "<encoded op>"
  operationCoder.decode("<encoded op>") returns mockedOp
  
  val messageCoder = new MessageCoder {
    override val operationCoder = MessageCoderSpec.operationCoder
  }

  "MessageCoder" should {
    "encode and decode ConcurrentOperationMessage" in {
      val originalMsg = new OperationContext(mockedOp, 123, 234)
      val encodedMsg = messageCoder.encode(originalMsg)
      val decodedMsg = messageCoder.decode(encodedMsg)
      originalMsg must equalTo(decodedMsg)
    }
  }
}
