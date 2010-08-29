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

import javax.swing.event.{DocumentListener, DocumentEvent}

class TextComponentDocumentListener(onInsert: List[(Int, Char)] => Unit,
                                    onDelete: List[Int] => Unit) extends DocumentListener {
  def changedUpdate(e: DocumentEvent) = error("changedUpdate")
  def insertUpdate(e: DocumentEvent) {
    val document = e.getDocument
    val start = e.getOffset
    val end = e.getOffset + e.getLength
    onInsert((start until end).toList.map { i => (i, document.getText(i, 1)(0)) })
  }
  def removeUpdate(e: DocumentEvent) {
    val start = e.getOffset
    val end = e.getOffset + e.getLength
    onDelete((start until end).map { _ => start }.toList)
  }
}

