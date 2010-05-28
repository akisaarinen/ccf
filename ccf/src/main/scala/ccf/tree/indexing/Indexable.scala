package ccf.tree.indexing

trait Indexable {
  def shiftForInsertOf(other: Indexable) = if (isAffectedBy(other)) shift(1, other) else this
  def shiftForRemoveOf(other: Indexable) = if (isAffectedBy(other)) shift(-1, other) else this

  def shift(count: Int, other: Indexable) : Indexable
  def shift(count: Int) : Indexable = shift(count, this)
  def <(other: Indexable) : Boolean
  def ==(other: Indexable) : Boolean
  def isAffectedBy(other: Indexable) : Boolean
  def isDescendantOf(other: Indexable) : Boolean

  def <=(other: Indexable) = (this < other || this == other)
  def >(other: Indexable) = !(this <= other)
  def >=(other: Indexable) = (this > other || this == other)

  def encode: Any
}
