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

package textapp

import ccf.tree.indexing.TreeIndex
import java.security.MessageDigest
import ccf.tree.operation.{NoOperation, TreeOperation, InsertOperation, DeleteOperation}
import java.io.Serializable

class TextDocument(initialText: String) extends Serializable {
  private val buffer = new StringBuffer(initialText)
  private val md = MessageDigest.getInstance("MD5")
  def applyOp(op: TreeOperation): Unit = op match {
    case InsertOperation(TreeIndex(i), Elem(c)) => buffer.insert(i, c)
    case DeleteOperation(TreeIndex(i)) => buffer.deleteCharAt(i)
    case NoOperation() =>
    case _ => error("Unknown operation " + op)
  }
  def text = buffer.toString
  def hash: String = {
    md.reset
    md.update(text.getBytes)
    md.digest.map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }
}

