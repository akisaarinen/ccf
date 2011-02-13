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

package textapp.document

import ccf.tree.TreeNode
import ccf.tree.operation.{TreeOperation, Modifier, TreeOperationDecoder}

class TextAppOperationDecoder extends TreeOperationDecoder {
  protected def parseNode(encodedValue: Any): TreeNode = {
    new CharacterNode(encodedValue.asInstanceOf[String].charAt(0))
  }

  protected def parseModifier(modifier: Any): Modifier = {
    modifier match {
      //case "nop" => NopModifier()
      case _ => error("TreeOperationDecoder#parseModifier: Unkown modifier type " + modifier)
    }
  }

  protected def parseApplicationOperations(operation: String, opMap: Map[String, String]): TreeOperation = {
    error("TreeOperationDecoder#decode: Unknown operation type " + operation)
  }
}