package textapp

import textapp.server.ServerApp
import textapp.client.ClientApp

object TextAppMain {
  private val defaultHost = "localhost"
  private val defaultPort = "9999"

  def main(args: Array[String]) {
    val clap = new CommandLineArgumentProcessor(args)
    val host = clap.getParam("host", "h").getOrElse(defaultHost)
    val port = clap.getParam("port", "p").getOrElse(defaultPort)

    clap.getParam("server", "s") match {
      case Some(_) => runServer(port.toInt)
      case None => runClient(host, port.toInt)
    }
  }

  private def runServer(port: Int) {
    new ServerApp(port)
    println("Server application started at port %d".format(port))
  }

  private def runClient(host: String, port: Int) {
    val clientApp = new ClientApp(host, port)
    println("Client application started'")
  }
}

