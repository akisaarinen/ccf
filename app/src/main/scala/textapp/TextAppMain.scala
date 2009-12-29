package textapp

object TextAppMain {
  def main(args: Array[String]) {
    val serverApp = new ServerApp
    val clientApp = new ClientApp
    println("Text Application started")
  }
}

