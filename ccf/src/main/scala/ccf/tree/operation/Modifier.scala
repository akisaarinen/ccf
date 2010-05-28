package ccf.tree.operation

import ccf.tree.TreeNode

trait Modifier {
  def apply(node: TreeNode)
  def encode: Any
}

case class NopModifier() extends Modifier {
  def apply(node: TreeNode) {}
  def encode: Any = "nop"
}
