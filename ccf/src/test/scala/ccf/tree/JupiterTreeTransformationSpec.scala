package ccf.tree

import org.specs.Specification
import ccf.tree.operation._
import ccf.tree.indexing.TreeIndex
import ccf.JupiterTransformer

object JupiterTreeTransformationSpec extends Specification {
  val t : JupiterTransformer[TreeOperation] = JupiterTreeTransformation
  "JupiterTreeTransformation" should {
    "create no-op" in {
      t.createNoOp must equalTo(NoOperation())
    }
  }

  "JupiterTreeTransformation with local move and remote update" should {
    "not affect update if move is in front of the update" in {
      val localOp = move(0, 1)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(remoteOp)
    }

    "not affect update if move is after the update" in {
      val localOp = move(3, 4)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(remoteOp)
    }

    "track update when updating node has been moved forward" in {
      val localOp = move(2, 8)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(update(8, "A"))
    }

    "track update when updating node has been moved backward" in {
      val localOp = move(2, 0)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(update(0, "A"))
    }
  }

  private def update(i: Int, name: String) = new UpdateAttributeOperation(TreeIndex(i), name, NopModifier())
  private def move(src: Int, dst: Int) = new MoveOperation(TreeIndex(src), TreeIndex(dst))

  private def transformPrimary(localOp: TreeOperation, remoteOp: TreeOperation): TreeOperation = {
    t.transformRemoteOpForLocalExecution(localOp, remoteOp, true)
  }

  private def transformSecondary(localOp: TreeOperation, remoteOp: TreeOperation): TreeOperation = {
    t.transformRemoteOpForLocalExecution(localOp, remoteOp, false)
  }

  private def transformBoth(localOp: TreeOperation, remoteOp: TreeOperation): TreeOperation = {
    val asPrimary = transformPrimary(localOp, remoteOp)
    val asSecondary = transformSecondary(localOp, remoteOp)
    asPrimary must equalTo(asSecondary)
    asPrimary
  }
}
