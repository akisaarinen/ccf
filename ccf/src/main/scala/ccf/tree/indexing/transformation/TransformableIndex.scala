package ccf.tree.indexing.transformation

import ccf.tree.indexing.Indexable

abstract sealed case class TransformableIndex(val index: Indexable)

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
