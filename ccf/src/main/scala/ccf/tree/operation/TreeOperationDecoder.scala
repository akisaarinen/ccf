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
import ccf.transport.BASE64EncodingSerializer

abstract class TreeOperationDecoder extends OperationDecoder {
  def decode(op: Any): TreeOperation = {
    op match {
      case string: String => decode(string)
      case stringMap: Map[String, String] => decode(stringMap)
    }
  }
  def decode(op: String): TreeOperation = BASE64EncodingSerializer.deserialize(op)
  def decode(opMap: Map[String, String]): TreeOperation = {
    opMap.get("type") match {
      case Some("NoOperation") => NoOperation()
      case Some("InsertOperation") => InsertOperation(parseIndex(opMap("index")), parseNode(opMap("node")))
      case Some("DeleteOperation") => DeleteOperation(parseIndex(opMap("index")))
      case Some("MoveOperation") => MoveOperation(parseIndex(opMap("sourceIndex")), parseIndex(opMap("targetIndex")))
      case Some(t) => error("TreeOperationDecoder#decode: Unknown operation type " + t)
      case None => error("TreeOperationDecoder#decode: No type found for operation")
    }
  }

  private def parseIndex(encodedValue: Any): TreeIndex = {
    TreeIndex(encodedValue.asInstanceOf[List[Int]]: _*)
  }
  protected def parseNode(encodedValue: Any): TreeNode
}

class DefaultTreeOperationDecoder extends TreeOperationDecoder {
  protected def parseNode(encodedValue: Any): TreeNode = {
    DefaultNode(encodedValue.asInstanceOf[String])
  }
}

case class DefaultNode(value: String) extends TreeNode {
  override def encode = value
}

object TreeOperationDecoder extends DefaultTreeOperationDecoder