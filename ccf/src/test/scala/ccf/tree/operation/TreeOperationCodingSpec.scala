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

package ccf.tree.operation

import org.specs.Specification
import ccf.tree.indexing.TreeIndex
object TreeOperationCodingSpec extends Specification {
  val decoder = new TreeOperationDecoder

  "Encoder and decoder" should {
    val index = TreeIndex(1,2,3)
    val node = TestNode("lol")
    "encode and decode no operation" in {
      val original = NoOperation()
      encodeAndDecode(original) must equalTo(original)
    }
    "encode and decode insert" in {
      val original = InsertOperation(index, node)
      encodeAndDecode(original) must equalTo(original)
    }
    "encode and decode delete" in {
      val original = DeleteOperation(index)
      encodeAndDecode(original) must equalTo(original)
    }
    "encode and decode move" in {
      val src = index
      val dst = TreeIndex(4,5,6)
      val original = MoveOperation(src, dst)
      encodeAndDecode(original) must equalTo(original)
    }
    "encode and decode update" in {
      val original = UpdateAttributeOperation(index, "someAttr", new TestModifier("foo"))
      encodeAndDecode(original) must equalTo(original)
    }
  }

  private def encodeAndDecode(op: TreeOperation): TreeOperation = {
    val encoded = op.encode
    decoder.decode(encoded)
  }
}