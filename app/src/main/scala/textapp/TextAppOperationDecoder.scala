package textapp

import ccf.tree.TreeNode
import ccf.tree.operation.{TreeOperation, Modifier, TreeOperationDecoder}

class TextAppOperationDecoder extends TreeOperationDecoder {
  protected def parseNode(encodedValue: Any): TreeNode = {
    new Elem(encodedValue.asInstanceOf[String].charAt(0))
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