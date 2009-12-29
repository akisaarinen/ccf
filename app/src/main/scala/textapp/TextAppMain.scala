package textapp

import javax.swing.UIManager

object TextAppMain {
  def main(args: Array[String]) {
    val serverApp = new ServerApp
    val clientApp = new ClientApp
    println("initialized")
  }
}

