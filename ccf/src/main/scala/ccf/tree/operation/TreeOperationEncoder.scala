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

import ccf.session.OperationEncoder

class TreeOperationEncoder extends OperationEncoder {
  def encode(op: TreeOperation) = {
    val typeString = op.getClass.getSimpleName
    Map("type" -> typeString) ++ encodedContents(op)
  }

  private def encodedContents(op: TreeOperation): Map[String, Any] = op match {
    case NoOperation() => Map()
    case InsertOperation(index, node) => Map("index" -> index.encode, "node" -> node.encode)
    case DeleteOperation(index) => Map("index" -> index.encode)
    case MoveOperation(srcIndex, dstIndex) => Map("src" -> srcIndex.encode, "dst" -> dstIndex.encode)
    case UpdateAttributeOperation(index, attribute, modifier) => Map("index" -> index.encode, "attr" -> attribute, "modifier" -> modifier.encode)
    case _ => error("Unable to encode given operation " + op.toString)
  }
}

object TreeOperationEncoder extends TreeOperationEncoder