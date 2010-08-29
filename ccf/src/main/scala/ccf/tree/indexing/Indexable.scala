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
