package textapp.client

import javax.swing.SwingUtilities

object Utils {
  def invokeAndWait(block: () => Unit) {
    SwingUtilities.invokeAndWait(new Runnable {
      override def run = block.apply
    })
  }
}
