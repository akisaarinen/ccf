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

