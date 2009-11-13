package ccf.tree.indexing

case class UndefinedIndex() extends Indexable {
  def shift(count: Int, other: Indexable) = this
  def <(other: Indexable) = true
  def ==(other: Indexable) = false
  def isAffectedBy(other: Indexable) = false
  def isDescendantOf(other: Indexable) = false
}
