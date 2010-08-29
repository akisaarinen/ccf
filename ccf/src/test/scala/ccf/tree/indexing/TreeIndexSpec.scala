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

import org.specs.Specification

object TreeIndexSpec extends Specification {
  "Any TreeIndex" should {
    "be equal to itself after initialization" in {
      TreeIndex(0) mustEqual TreeIndex(0)
      TreeIndex(0,1) mustEqual TreeIndex(0,1)
      TreeIndex(0,1,2) mustEqual TreeIndex(0,1,2)
    }
    "not be equal to different indices" in {
      TreeIndex(0) must not equalTo(TreeIndex())
      TreeIndex(0) must not equalTo(TreeIndex(1))
      TreeIndex(0) must not equalTo(TreeIndex(0,0))
    }
    "be greater than its parent" in {
      TreeIndex(0,1) > TreeIndex(0) must beTrue
      TreeIndex(0,1,2) > TreeIndex(0,1) must beTrue
    }
    "be greater than previous index in same level" in {
      TreeIndex(1) > TreeIndex(0) must beTrue
      TreeIndex(0,2) > TreeIndex(0,1) must beTrue
    }
    "construct its parent correctly" in {
      TreeIndex(1).parent mustEqual TreeIndex()
      TreeIndex(2,3).parent mustEqual TreeIndex(2)
      TreeIndex(5,6,7).parent mustEqual TreeIndex(5,6)
    }
    "ignore shift when shifted with global parent" in {
      TreeIndex().shift(1, TreeIndex()) mustEqual TreeIndex()
      TreeIndex(1).shift(1, TreeIndex()) mustEqual TreeIndex(1)
      TreeIndex(1,2).shift(1, TreeIndex()) mustEqual TreeIndex(1,2)
    }
    "shift first level up when shifted with first level index" in {
      val firstLevelIndex = TreeIndex(0)
      TreeIndex(1).shift(1, firstLevelIndex) mustEqual TreeIndex(2)
      TreeIndex(2,3).shift(1, firstLevelIndex) mustEqual TreeIndex(3,3)
      TreeIndex(4,5,6).shift(1, firstLevelIndex) mustEqual TreeIndex(5,5,6)
    }
    "shift first level down when shifted with first level index" in {
      val firstLevelIndex = TreeIndex(0)
      TreeIndex(1).shift(-1, firstLevelIndex) mustEqual TreeIndex(0)
      TreeIndex(2,3).shift(-1, firstLevelIndex) mustEqual TreeIndex(1,3)
      TreeIndex(4,5,6).shift(-1, firstLevelIndex) mustEqual TreeIndex(3,5,6)
    }
    "shift second level up when shifted with second level index" in {
      val secondLevelIndex = TreeIndex(0,1)
      TreeIndex(1).shift(1, secondLevelIndex) mustEqual TreeIndex(1)
      TreeIndex(2,3).shift(1, secondLevelIndex) mustEqual TreeIndex(2,4)
      TreeIndex(4,5,6).shift(1, secondLevelIndex) mustEqual TreeIndex(4,6,6)
    }
    "shift second level down when shifted with second level index" in {
      val secondLevelIndex = TreeIndex(0,1)
      TreeIndex(1).shift(-1, secondLevelIndex) mustEqual TreeIndex(1)
      TreeIndex(2,3).shift(-1, secondLevelIndex) mustEqual TreeIndex(2,2)
      TreeIndex(4,5,6).shift(-1, secondLevelIndex) mustEqual TreeIndex(4,4,6)
    }
    "be descendent of global parent" in {
      TreeIndex(1).isDescendantOf(TreeIndex()) must beTrue
      TreeIndex(1,2).isDescendantOf(TreeIndex()) must beTrue
    }
    "be descendent of all of its parents" in {
      TreeIndex(1,2,3,4).isDescendantOf(TreeIndex()) must beTrue
      TreeIndex(1,2,3,4).isDescendantOf(TreeIndex(1)) must beTrue
      TreeIndex(1,2,3,4).isDescendantOf(TreeIndex(1,2)) must beTrue
      TreeIndex(1,2,3,4).isDescendantOf(TreeIndex(1,2,3)) must beTrue
    }
    "not be descendent of items in the same level" in {
      TreeIndex(1,2).isDescendantOf(TreeIndex(1,1)) must beFalse
      TreeIndex(1,2).isDescendantOf(TreeIndex(1,2)) must beFalse
      TreeIndex(1,2).isDescendantOf(TreeIndex(1,3)) must beFalse
    }
    "not be descendent of its children" in {
      TreeIndex(1,2).isDescendantOf(TreeIndex(1,2,1)) must beFalse
      TreeIndex(1,2).isDescendantOf(TreeIndex(1,2,2)) must beFalse
    }
    "be incremented" in {
      TreeIndex(1).increment(1) must equalTo(TreeIndex(2))
      TreeIndex(1,2).increment(2) must equalTo(TreeIndex(1,4))
      TreeIndex(4).increment(-1) must equalTo(TreeIndex(3))
    }
    "be decremented" in {
      TreeIndex(1).decrement(1) must equalTo(TreeIndex(0))
      TreeIndex(1,2).decrement(2) must equalTo(TreeIndex(1,0))
      TreeIndex(4).decrement(-1) must equalTo(TreeIndex(5))
    }
  }
}
