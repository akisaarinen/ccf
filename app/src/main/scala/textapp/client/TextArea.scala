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

package textapp.client

import ccf.tree.indexing.TreeIndex
import textapp.Elem
import java.awt.{Color, Dimension}
import javax.swing.JTextPane
import javax.swing.text.{StyleConstants, SimpleAttributeSet, StyleContext}
import ccf.tree.operation.{NoOperation, TreeOperation, InsertOperation, DeleteOperation}

class TextArea(initialText: String,
               onInsert: List[(Int, Char)] => Unit, 
               onDelete: List[Int] => Unit) extends JTextPane {
  private val defaultStyleContext = StyleContext.getDefaultStyleContext
  private val remoteEditColor = new Color(255, 0, 0)
  private val remoteEditAttributes = defaultStyleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, remoteEditColor);
  private val localEditColor = new Color(0, 150, 0)
  private val localEditAttributes = defaultStyleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, localEditColor);

  setPreferredSize(new Dimension(600, 400))
  setText(initialText)
  val listener = new TextComponentDocumentListener(onInsert, onDelete)
  getDocument.addDocumentListener(listener)

  override def getInputAttributes = {
    val originalInputAttributes = super.getInputAttributes
    StyleConstants.setForeground(originalInputAttributes, localEditColor)
    originalInputAttributes
  }

  def applyOp(op: TreeOperation) {
    getDocument.removeDocumentListener(listener)
    op match {
      case InsertOperation(TreeIndex(i), Elem(c)) =>         
        getDocument.insertString(i, c.toString, remoteEditAttributes)
      case DeleteOperation(TreeIndex(i)) =>
        getDocument.remove(i, 1)
      case NoOperation() =>
      case _ => error("unable to apply operation to text area ('%s')".format(op))
    }
    getDocument.addDocumentListener(listener)
  }
}
