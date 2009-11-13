package ccf.tree.operation

import ccf.tree.TreeNode

trait Modifier {
  def apply(node: TreeNode)
}

case class NopModifier() extends Modifier {
  def apply(node: TreeNode) {}
}

