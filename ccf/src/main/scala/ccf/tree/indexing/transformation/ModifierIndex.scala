package ccf.tree.indexing.transformation

import ccf.tree.indexing.Indexable

sealed abstract case class ModifierIndex(index: Indexable)
case class InsertedIndex(override val index: Indexable) extends ModifierIndex(index)
case class DeletedIndex(override val index: Indexable) extends ModifierIndex(index)
