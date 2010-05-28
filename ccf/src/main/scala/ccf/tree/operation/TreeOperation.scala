package ccf.tree.operation

import ccf.tree.indexing.{Indexable, UndefinedIndex}
import ccf.operation.Operation
import ccf.tree.TreeNode

abstract sealed case class TreeOperation(val index: Indexable) extends Operation

case class NoOperation() extends TreeOperation(UndefinedIndex()) {
  def encode = "nop"
}

case class InsertOperation(override val index: Indexable, val node: TreeNode) extends TreeOperation(index) {
  def encode = Map("index" -> index.encode, "node" -> node.encode)
}

case class DeleteOperation(override val index: Indexable) extends TreeOperation(index) {
  def encode = Map("index" -> index.encode)
}

case class MoveOperation(val sourceIndex: Indexable, val targetIndex: Indexable) extends TreeOperation(sourceIndex) {
  def encode = Map("srcIndex" -> sourceIndex, "dstIndex" -> targetIndex)
}

case class UpdateAttributeOperation(override val index: Indexable, val attribute: String, val modifier: Modifier) extends TreeOperation(index) {
  def encode = Map("index" -> index, "attribute" -> attribute, "modifier" -> modifier.encode)
}
