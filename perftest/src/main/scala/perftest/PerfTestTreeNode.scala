package perftest

import ccf.tree.TreeNode

class PerfTestTreeNode(name: String) extends TreeNode {
  def encode = name
}