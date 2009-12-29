package ccf.tree.indexing.transformation

sealed abstract case class ModifierIndex(index: Indexable)
case class InsertedIndex(override val index: Indexable) extends ModifierIndex(index)
case class DeletedIndex(override val index: Indexable) extends ModifierIndex(index)
