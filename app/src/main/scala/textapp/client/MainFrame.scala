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
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import textapp.client.jgoodies.FormsPanel
import textapp.{TextDocument, Elem}
import javax.swing.{JLabel, JFrame}
import java.awt.Color

class MainFrame(hostname: String, port: Int, document: TextDocument, sendToServer: TreeOperation => Unit) extends JFrame("libccf test application") {
  val textArea = new TextArea(document.text, onInsert, onDelete)
  val mainPanel = new MainPanel(textArea)
  getContentPane().add(mainPanel)

  def applyOp(op: TreeOperation) {
    textArea.applyOp(op)
  }

  private def onInsert(items: List[(Int, Char)]) {
    items.foreach { case (i, c) => 
      sendToServer(InsertOperation(TreeIndex(i), Elem(c)))
    }
  }

  private def onDelete(items: List[Int]) {
    items.foreach { i =>
      sendToServer(DeleteOperation(TreeIndex(i)))
    }
  }

  class MainPanel(textArea: TextArea) extends FormsPanel("pref", "pref, pref") {
    val infoPanel = new FormsPanel("pref", "20px, 14px, 14px, 8px") {
      val genericInfo = new JLabel("Connected to server at '%s:%d' using HTTP".format(hostname, port))
      val ownTextInfo = new JLabel("* Own text is shown with green color")
      val otherTextInfo = new JLabel("* Others' text is shown with red color")
      ownTextInfo.setForeground(new Color(0, 150, 0))
      otherTextInfo.setForeground(new Color(255, 0, 0))

      add(genericInfo, xy(1,1))
      add(ownTextInfo, xy(1,2))
      add(otherTextInfo, xy(1,3))
    }
    add(infoPanel, xy(1,1))
    add(textArea, xy(1,2))
  }
}
