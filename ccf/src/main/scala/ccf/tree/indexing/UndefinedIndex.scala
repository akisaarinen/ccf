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

case class UndefinedIndex() extends Indexable {
  def shift(count: Int, other: Indexable) = this
  def <(other: Indexable) = true
  def ==(other: Indexable) = false
  def isAffectedBy(other: Indexable) = false
  def isDescendantOf(other: Indexable) = false
  def encode: Any = "undefined"
}
