package ccf.tree.indexing

case class TreeIndex(val indexPath: Int*) extends Indexable {
  def shift(count: Int, other: Indexable) = other match {
    case other: TreeIndex if (other.level > level) => TreeIndex(indexPath: _*)
    case other: TreeIndex if (other.level == 0) => TreeIndex(indexPath: _*)
    case other: TreeIndex => {
      val shiftLevel = other.indexPath.size
      val shiftedPath =
        indexPath.toList.take(shiftLevel - 1) ++
        List(indexPath(shiftLevel-1) + count) ++
        indexPath.toList.drop(shiftLevel)
      TreeIndex(shiftedPath: _*)
    }
    case _ => TreeIndex(indexPath: _*)
  }

  def <(other: Indexable) = matchSupported(other) {
    case UndefinedIndex() => false
    case other: TreeIndex if (level < other.level) => this < other.parent.shift(1)
    case other: TreeIndex if (level == other.level) => indexInLevel < other.indexInLevel
    case other: TreeIndex if (level > other.level) => parent.shift(1) < other
  }

  def ==(other: Indexable) = matchSupported(other) {
    case UndefinedIndex() => false
    case other: TreeIndex => indexPath.toList == other.indexPath.toList // comparison needs to be done as lists
  }

  def isAffectedBy(other: Indexable) = matchSupported(other) {
    case other: TreeIndex if (other.level < level) => true
    case other: TreeIndex if (other.level == level) => parent == other.parent // if parents match
    case other: TreeIndex if (other.level > level) => false
    case UndefinedIndex() => false
  }

  def isDescendantOf(other: Indexable) = matchSupported(other) {
    case UndefinedIndex() => false
    case other: TreeIndex if (other.level >= level) =>false
    case other: TreeIndex if (other.level == level-1) => other == parent
    case other: TreeIndex if (other.level < level) => parent.isDescendantOf(other)
  }

  protected def matchSupported(other: Indexable)(pf: PartialFunction[Indexable, Boolean]) = {
    if (pf isDefinedAt other) pf(other) else throw new UnsupportedOperationException("Unsupported index combination with "+other)
  }

  def parent = if (level <= 1) TreeIndex() else TreeIndex(indexPath.toList.dropRight(1) : _*)
  def append(index: Int) = TreeIndex((indexPath.toList ::: List(index)): _*)
  def append(backlogTreeIndex: TreeIndex) = TreeIndex((indexPath.toList ::: backlogTreeIndex.indexPath.toList): _*)

  def level = indexPath.size
  def indexInLevel = if (level == 0) 0 else indexPath.last
  def increment(count: Int) = parent.append(indexInLevel + count)
  def decrement(count: Int) = parent.append(indexInLevel - count)

  def encode: List[Int] = indexPath.toList
}
