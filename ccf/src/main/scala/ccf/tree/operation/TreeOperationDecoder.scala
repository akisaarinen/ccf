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
      case stringMap: Map[String, String] => decode(stringMap)
      case _ => error("TreeOperationDecoder#decode: Unable to decode " +op)
    }
  }
  def decode(opMap: Map[String, String]): TreeOperation = {
    opMap.get("type") match {
      case Some("NoOperation") => NoOperation()
      case Some("InsertOperation") => InsertOperation(parseIndex(opMap("index")), parseNode(opMap("node")))
      case Some("DeleteOperation") => DeleteOperation(parseIndex(opMap("index")))
      case Some("MoveOperation") => MoveOperation(parseIndex(opMap("sourceIndex")), parseIndex(opMap("targetIndex")))
      case Some("UpdateAttributeOperation") => UpdateAttributeOperation(parseIndex(opMap("index")), opMap("attribute"), parseModifier(opMap("modifier")))
      case Some(t) => parseApplicationOperations(t, opMap)
      case None => error("TreeOperationDecoder#decode: No type found for operation")
    }
  }

  protected def parseApplicationOperations(operation: String, opMap: Map[String, String]): TreeOperation

  protected def parseIndex(encodedValue: Any): TreeIndex = {
    TreeIndex(encodedValue.asInstanceOf[List[Int]]: _*)
  } 
  protected def parseNode(encodedValue: Any): TreeNode
  protected def parseModifier(encodedValue: Any): Modifier
}

class DefaultTreeOperationDecoder extends TreeOperationDecoder {
  protected def parseNode(encodedValue: Any): TreeNode = {
    DefaultNode(encodedValue.asInstanceOf[String])
  }
  protected def parseModifier(modifier: Any): Modifier = {
    modifier match {
      case "nop" => NopModifier()
      case _ => error("TreeOperationDecoder#parseModifier: Unkown modifier type "+modifier)
    }
  }

  protected def parseApplicationOperations(operation: String, opMap: Map[String, String]): TreeOperation = {
    error("TreeOperationDecoder#decode: Unknown operation type " + operation)
  }
}

case class DefaultNode(value: String) extends TreeNode {
  override def encode = value
}

object TreeOperationDecoder extends DefaultTreeOperationDecoder