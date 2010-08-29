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

package textapp.client.jgoodies

import javax.swing.JPanel
import com.jgoodies.forms.layout.{CellConstraints, FormLayout}
import com.jgoodies.forms.layout.CellConstraints.Alignment
import java.awt.Component

class FormsPanel(colSpec: String, rowSpec: String) extends JPanel with CellConstraining {
  setLayout(new FormLayout(colSpec, rowSpec))
  override def getLayout = super.getLayout.asInstanceOf[FormLayout]
  protected def add(component: Component, constraints: CellConstraints) = super.add(component, constraints)
}

trait CellConstraining {
  def leftAlign = CellConstraints.LEFT
  def rightAlign  = CellConstraints.RIGHT
  def fill = CellConstraints.FILL
  def left = CellConstraints.LEFT
  def center = CellConstraints.CENTER
  def top = CellConstraints.TOP
  def cc = new CellConstraints
  def xy(x: Int, y: Int) = xyw(x, y, 1)
  def xyw(x: Int, y: Int, colSpan: Int) = xywh(x, y, colSpan, 1)
  def xywh(x: Int, y: Int, colSpan: Int, rowSpan: Int) = xywhcr(x, y, colSpan, rowSpan, fill, fill)
  def xycr(x: Int, y: Int, colAlign: Alignment, rowAlign: Alignment) = xywhcr(x, y, 1, 1, colAlign, rowAlign)
  def xywhcr(x: Int, y: Int, colSpan: Int, rowSpan: Int, colAlign: Alignment, rowAlign: Alignment) = cc.xywh(x, y, colSpan, rowSpan, colAlign, rowAlign)
}
