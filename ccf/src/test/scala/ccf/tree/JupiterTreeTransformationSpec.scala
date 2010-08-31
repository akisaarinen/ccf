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

package ccf.tree

import org.specs.Specification
import ccf.tree.operation._
import ccf.tree.indexing.TreeIndex
import ccf.JupiterTransformer

object JupiterTreeTransformationSpec extends Specification {
  val t : JupiterTransformer = JupiterTreeTransformation
  "JupiterTreeTransformation" should {
    "create no-op" in {
      t.createNoOp must equalTo(NoOperation())
    }
  }

  "JupiterTreeTransformation with local insert in front of remote operation" should {
    val localOp = insert(1, "A")
    "move remote insert forward" in {
      val remoteOp = insert(2, "B")
      transformBoth(localOp, remoteOp) must equalTo(insert(3, "B"))
    }
    "move remote delete forward" in {
      val remoteOp = delete(2)
      transformBoth(localOp, remoteOp) must equalTo(delete(3))
    }
    "move remote update forward" in {
      val remoteOp = update(2, "B")
      transformBoth(localOp, remoteOp) must equalTo(update(3, "B"))
    }
    "move remote target index of move forward" in {
      val remoteOp = move(0, 7)
      transformBoth(localOp, remoteOp) must equalTo(move(0, 8))
    }
    "move source index of remote move forward" in {
      val remoteOp = move(5, 0)
      transformBoth(localOp, remoteOp) must equalTo(move(6, 0))
    }
    "move source and target index of remote move forward" in {
      val remoteOp = move(5, 7)
      transformBoth(localOp, remoteOp) must equalTo(move(6, 8))
    }
  }

  "JupiterTreeTransformation with local delete in front of remote operation" should {
    val localOp = delete(1)
    "move remote insert backward" in {
      val remoteOp = insert(2, "B")
      transformBoth(localOp, remoteOp) must equalTo(insert(1, "B"))
    }
    "move remote delete backward" in {
      val remoteOp = delete(2)
      transformBoth(localOp, remoteOp) must equalTo(delete(1))
    }
    "move remote update backward" in {
      val remoteOp = update(2, "B")
      transformBoth(localOp, remoteOp) must equalTo(update(1, "B"))
    }
    "move target index of remote move backward" in {
      val remoteOp = move(0, 7)
      transformBoth(localOp, remoteOp) must equalTo(move(0, 6))
    }
    "move source index of remote move backward" in {
      val remoteOp = move(5, 0)
      transformBoth(localOp, remoteOp) must equalTo(move(4, 0))
    }
    "move source and target index of remote move backward" in {
      val remoteOp = move(5, 7)
      transformBoth(localOp, remoteOp) must equalTo(move(4, 6))
    }
  }

  "JupiterTreeTransformation with local delete that invalidates remote operations" should {
    val localOp = delete(1)
    "not invalidate insert to same index" in {
      val remoteOp = insert(1, "B")
      transformBoth(localOp, remoteOp) must equalTo(remoteOp)
    }
    "not invalidate insert to index after delete" in {
      val remoteOp = insert(2, "B")
      transformBoth(localOp, remoteOp) must equalTo(insert(1, "B"))
    }
    "invalidate remote delete to same index" in {
      val remoteOp = delete(1)
      transformBoth(localOp, remoteOp) must equalTo(NoOperation())
    }
    "invalidate remote update to same index" in {
      val remoteOp = update(1, "B")
      transformBoth(localOp, remoteOp) must equalTo(NoOperation())
    }
    "invalidate remote move from same source index to forward" in {
      val remoteOp = move(1, 9)
      transformBoth(localOp, remoteOp) must equalTo(NoOperation())
    }
    "invalidate remote move from same source index to backward" in {
      val remoteOp = move(1, 0)
      transformBoth(localOp, remoteOp) must equalTo(NoOperation())
    }
  }

  "JupiterTreeTransformation with local move and remote operations to other nodes" should {
    "not affect remote update if move is in front of the update" in {
      val localOp = move(0, 1)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(remoteOp)
    }

    "not affect remote update if move is after the update" in {
      val localOp = move(3, 4)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(remoteOp)
    }
  }

  "JupiterTreeTransformation with local move and remote operations targeting the moved node" should {
    "track remote update when node has been moved forward" in {
      val localOp = move(2, 8)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(update(8, "A"))
    }

    "track remote update when node has been moved backward" in {
      val localOp = move(2, 0)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(update(0, "A"))
    }

    "track remote update when node has been moved in-place" in {
      val localOp = move(2, 2)
      val remoteOp = update(2, "A")
      transformBoth(localOp, remoteOp) must equalTo(update(2, "A"))
    }

    "track remote delete when node has been moved forward" in {
      val localOp = move(2, 8)
      val remoteOp = delete(2)
      transformBoth(localOp, remoteOp) must equalTo(delete(8))
    }

    "track remote delete when node has been moved backward" in {
      val localOp = move(2, 0)
      val remoteOp = delete(2)
      transformBoth(localOp, remoteOp) must equalTo(delete(0))
    }

    "track remote delete when node has been moved in-place" in {
      val localOp = move(2, 2)
      val remoteOp = delete(2)
      transformBoth(localOp, remoteOp) must equalTo(delete(2))
    }

    "track remote move when node has been moved forward and remote is primary" in {
      val localOp = move(2, 8)
      val remoteOp = move(2, 4)
      transformSecondary(localOp, remoteOp) must equalTo(move(8, 4))
    }

    "track remote move when node has been moved backward and remote is primary" in {
      val localOp = move(2, 0)
      val remoteOp = move(2, 4)
      transformSecondary(localOp, remoteOp) must equalTo(move(0, 4))
    }

    "track remote move when node has been moved in-place" in {
      val localOp = move(2, 2)
      val remoteOp = move(2, 4)
      transformBoth(localOp, remoteOp) must equalTo(move(2, 4))
    }

    "invalidate remote move when node has been moved forward and local is primary" in {
      val localOp = move(2, 8)
      val remoteOp = move(2, 4)
      transformPrimary(localOp, remoteOp) must equalTo(NoOperation())
    }

    "invalidate remote move when node has been moved backward and local is primary" in {
      val localOp = move(2, 0)
      val remoteOp = move(2, 4)
      transformPrimary(localOp, remoteOp) must equalTo(NoOperation())
    }
  }

  private def insert(i: Int, name: String) = new InsertOperation(TreeIndex(i), Node(name))
  private def delete(i: Int) = new DeleteOperation(TreeIndex(i))
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

  private case class Node(name: String) extends TreeNode {
    def encode: Any = error("not implemented")
  }
}
