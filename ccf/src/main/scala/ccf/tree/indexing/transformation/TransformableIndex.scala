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

package ccf.tree.indexing.transformation

import ccf.tree.indexing.Indexable

abstract sealed class TransformableIndex(val index: Indexable)

case class SourceIndex(val source: Indexable) extends TransformableIndex(source) {
  def transformForInsert(inserted: InsertedIndex) = {
    if (inserted.index <= source) SourceIndex(source.shiftForInsertOf(inserted.index))
    else this
  }

  def transformForRemove(removed: DeletedIndex) = {
    if (removed.index < source) SourceIndex(source.shiftForRemoveOf(removed.index))
    else this
  }
}

case class TargetIndex(val target: Indexable) extends TransformableIndex(target) {
  def transformForInsert(inserted: InsertedIndex, insertedIsPrimary: Boolean) = {
    if (inserted.index < target) TargetIndex(target.shiftForInsertOf(inserted.index))
    else if (inserted.index == target && insertedIsPrimary) TargetIndex(target.shiftForInsertOf(inserted.index))
    else this
  }

  def transformForRemove(removed: DeletedIndex) = {
    if (removed.index < target) TargetIndex(target.shiftForRemoveOf(removed.index))
    else this
  }
}
