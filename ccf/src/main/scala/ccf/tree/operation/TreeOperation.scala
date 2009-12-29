package ccf.tree.operation

import ccf.operation.Operation
import ccf.tree.TreeNode
import ccf.tree.indexing._

abstract sealed case class TreeOperation(val index: Indexable) extends Operation

case class NoOperation() extends TreeOperation(UndefinedIndex())
case class InsertOperation(override val index: Indexable, val node: TreeNode) extends TreeOperation(index)
case class DeleteOperation(override val index: Indexable) extends TreeOperation(index)
case class MoveOperation(val sourceIndex: Indexable, val targetIndex: Indexable) extends TreeOperation(sourceIndex)
case class UpdateAttributeOperation(override val index: Indexable, val attribute: String, val modifier: Modifier) extends TreeOperation(index)
