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

import ccf.session.OperationDecoder
import ccf.tree.indexing.TreeIndex
import ccf.tree.TreeNode

abstract class TreeOperationDecoder extends OperationDecoder {
  def decode(any: Any): TreeOperation = {
    val map = any.asInstanceOf[Map[String, Any]]
    map.get("type") match {
      case Some("NoOperation") => NoOperation()
      case Some("InsertOperation") => InsertOperation(parseIndex(map("index")), parseNode(map("node")))
      case Some("DeleteOperation") => DeleteOperation(parseIndex(map("index")))
      case Some("MoveOperation") => MoveOperation(parseIndex(map("src")), parseIndex(map("dst")))
      case Some("UpdateAttributeOperation") => UpdateAttributeOperation(parseIndex(map("index")), map("attr").asInstanceOf[String], parseModifier(map("modifier")))
      case Some(t) => error("TreeOperationDecoder#decode: Unknown operation type " + t)
      case None => error("TreeOperationDecoder#decode: No type found for operation")
    }
  }

  private def parseIndex(encodedValue: Any): TreeIndex = {
    TreeIndex(encodedValue.asInstanceOf[List[Int]]: _*)
  }

  protected def parseNode(encodedValue: Any): TreeNode
  protected def parseModifier(encodedValue: Any): Modifier
}