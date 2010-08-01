package ccf.tree.operation

import ccf.tree.TreeNode

case class TestModifier(n: String) extends Modifier {
  def encode = n
  def apply(node: TreeNode) {}
}