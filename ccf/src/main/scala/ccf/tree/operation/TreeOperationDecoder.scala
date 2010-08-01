package ccf.tree.operation

import ccf.session.OperationDecoder
import ccf.tree.indexing.TreeIndex
import ccf.tree.TreeNode

abstract class TreeOperationDecoder extends OperationDecoder[TreeOperation] {
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