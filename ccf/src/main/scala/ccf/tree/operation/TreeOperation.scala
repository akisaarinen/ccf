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

package ccf.tree.operation

import ccf.tree.indexing.{Indexable, UndefinedIndex}
import ccf.tree.TreeNode

abstract sealed class TreeOperation(val index: Indexable) {
  def encode = TreeOperationEncoder.encode(this)
}

case class NoOperation() extends TreeOperation(UndefinedIndex())
case class InsertOperation(override val index: Indexable, val node: TreeNode) extends TreeOperation(index)
case class DeleteOperation(override val index: Indexable) extends TreeOperation(index)
case class MoveOperation(val sourceIndex: Indexable, val targetIndex: Indexable) extends TreeOperation(sourceIndex)
case class UpdateAttributeOperation(override val index: Indexable, val attribute: String, val modifier: Modifier) extends TreeOperation(index)
