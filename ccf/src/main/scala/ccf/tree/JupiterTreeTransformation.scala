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

import ccf.JupiterTransformer
import ccf.tree.indexing._
import ccf.tree.indexing.transformation._
import ccf.tree.operation._

object JupiterTreeTransformation extends JupiterTransformer {
  def createNoOp: TreeOperation = NoOperation()
  def transformRemoteOpForLocalExecution(localOp: TreeOperation, remoteOp: TreeOperation, localIsPrimary: Boolean) : TreeOperation = {
    if (isNoOp(localOp) || isNoOp(remoteOp)) {
      remoteOp
    } else {
      (localOp, remoteOp) match {
        // Handle special cases first
        case (DeleteOperation(locallyDeleted), _) if (isInvalidatedByDelete(locallyDeleted, remoteOp)) => NoOperation()
        case (MoveOperation(_, localTarget), DeleteOperation(remotelyDeleted)) if (isInvalidatedByDelete(remotelyDeleted, localOp)) => DeleteOperation(localTarget)
        case (localMove: MoveOperation, remoteMove: MoveOperation) if (localMove == remoteMove) => NoOperation()
        case (localMove: MoveOperation, remoteMove: MoveOperation) if (localMove.sourceIndex == remoteMove.sourceIndex) => {
          if (localIsPrimary) NoOperation() // local is primary, ignore server move
          else MoveOperation(localMove.targetIndex, remoteMove.targetIndex) // Server wins
        }
        case (localMove: MoveOperation, remoteUpdate: UpdateAttributeOperation) if (localMove.sourceIndex == remoteUpdate.index) => {
          UpdateAttributeOperation(localMove.targetIndex, remoteUpdate.attribute, remoteUpdate.modifier)
        }
        case (localUpdate: UpdateAttributeOperation, remoteUpdate: UpdateAttributeOperation) => {
          if (haveConflictingTargets(localUpdate, remoteUpdate) && localIsPrimary) NoOperation() // Ignore remote op if local is primary and there is conflict
          else remoteOp
        }
        // Default to the simple case
        case _ => transformSimpleCase(localOp, remoteOp, localIsPrimary)
      }
    }
  }

  private def haveConflictingTargets(firstOp: UpdateAttributeOperation, secondOp: UpdateAttributeOperation) = {
    firstOp.index == secondOp.index && firstOp.attribute == secondOp.attribute
  }

  private def isNoOp(op: TreeOperation) = op match {
    case NoOperation() => true
    case MoveOperation(s,t) if (s == t) => true
    case _ => false
  }

  private def getSourceIndex(op: TreeOperation) = op match {
    case NoOperation() => UndefinedIndex()
    case InsertOperation(_, _) => UndefinedIndex()
    case DeleteOperation(s) => s
    case MoveOperation(s,_) => s
    case UpdateAttributeOperation(s,_,_) => s
  }

  private def getTargetIndex(op: TreeOperation) = op match {
    case NoOperation() => UndefinedIndex()
    case InsertOperation(t, _) => t
    case DeleteOperation(_) => UndefinedIndex()
    case MoveOperation(_, t) => t
    case UpdateAttributeOperation(_,_,_) => UndefinedIndex()
  }

  private def getDeletedIndex(op: TreeOperation) = op match {
    case NoOperation() => UndefinedIndex()
    case InsertOperation(_, _) => UndefinedIndex()
    case DeleteOperation(s) => s
    case MoveOperation(s,_) => s
    case UpdateAttributeOperation(s,_,_) => UndefinedIndex()
  }

  private def getInsertedIndex(op: TreeOperation) = op match {
    case NoOperation() => UndefinedIndex()
    case InsertOperation(t, _) => t
    case DeleteOperation(_) => UndefinedIndex()
    case MoveOperation(_, t) => t
    case UpdateAttributeOperation(_,_,_) => UndefinedIndex()
  }

  private def transformSimpleCase(localOp: TreeOperation, remoteOp: TreeOperation, localIsPrimary: Boolean) : TreeOperation = {
    // Transform source from its definition context to the local op's context
    val src = SourceIndex(getSourceIndex(remoteOp))
            .transformForRemove(DeletedIndex(getDeletedIndex(localOp)))
            .transformForInsert(InsertedIndex(getInsertedIndex(localOp))).index

    // Transform target from its definition contest to the local op's context, here
    // we need to take into account that it was defined after the (possible)
    // remote source delete was executed.
    val tgt = TargetIndex(getTargetIndex(remoteOp))
            .transformForInsert(InsertedIndex(getDeletedIndex(remoteOp)), true)
            .transformForRemove(DeletedIndex(getDeletedIndex(localOp)))
            .transformForInsert(InsertedIndex(getInsertedIndex(localOp)), localIsPrimary)
            .transformForRemove(DeletedIndex(src)).index

    remoteOp match {
      case NoOperation() => throw new IllegalStateException("No-ops should not be transformed as the normal case")
      case InsertOperation(_, node) => InsertOperation(tgt, node)
      case DeleteOperation(_) => DeleteOperation(src)
      case MoveOperation(_,_) => MoveOperation(src, tgt)
      case UpdateAttributeOperation(_, attribute, modifier) => UpdateAttributeOperation(src, attribute, modifier)
    }
  }

  private def isInvalidatedByDelete(deleted: Indexable, op: TreeOperation) = op match {
    case InsertOperation(index, _) => index.isDescendantOf(deleted)
    case op: TreeOperation => op.index == deleted || op.index.isDescendantOf(deleted)
  }
}

